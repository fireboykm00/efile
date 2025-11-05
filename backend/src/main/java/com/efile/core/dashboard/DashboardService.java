package com.efile.core.dashboard;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.efile.core.casemanagement.Case;
import com.efile.core.casemanagement.CaseRepository;
import com.efile.core.casemanagement.CaseStatus;
import com.efile.core.casemanagement.dto.CaseResponse;
import com.efile.core.communication.Communication;
import com.efile.core.communication.CommunicationRepository;
import com.efile.core.communication.dto.CommunicationResponse;
import com.efile.core.dashboard.dto.DashboardSummary;
import com.efile.core.document.Document;
import com.efile.core.document.DocumentRepository;
import com.efile.core.document.DocumentStatus;
import com.efile.core.document.dto.DocumentResponse;
import com.efile.core.user.User;
import com.efile.core.user.UserRepository;
import com.efile.core.user.UserRole;
import com.efile.core.user.dto.UserSummary;

import jakarta.persistence.EntityNotFoundException;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final DocumentRepository documentRepository;
    private final CaseRepository caseRepository;
    private final CommunicationRepository communicationRepository;
    private final UserRepository userRepository;

    public DashboardService(
        DocumentRepository documentRepository,
        CaseRepository caseRepository,
        CommunicationRepository communicationRepository,
        UserRepository userRepository
    ) {
        this.documentRepository = documentRepository;
        this.caseRepository = caseRepository;
        this.communicationRepository = communicationRepository;
        this.userRepository = userRepository;
    }

    public DashboardSummary getDashboardSummary() {
        User currentUser = getCurrentUser();

        // Basic counts
        long pendingDocumentsCount = getPendingDocumentsCount(currentUser);
        long assignedCasesCount = getAssignedCasesCount(currentUser);
        long unreadCommunicationsCount = communicationRepository.countByRecipientAndIsRead(currentUser, false);
        long overdueCasesCount = 0; // TODO: Implement overdue calculation based on due dates

        // Extended metrics
        long totalDocuments = documentRepository.count();
        long approvedDocuments = documentRepository.findByStatus(DocumentStatus.APPROVED, PageRequest.of(0, 1)).getTotalElements();
        long rejectedDocuments = documentRepository.findByStatus(DocumentStatus.REJECTED, PageRequest.of(0, 1)).getTotalElements();
        long activeCases = caseRepository.findByStatus(CaseStatus.OPEN).size() +
                          caseRepository.findByStatus(CaseStatus.IN_PROGRESS).size();
        long unreadMessages = unreadCommunicationsCount;

        // Executive metrics (calculated)
        Double monthlyGrowth = calculateMonthlyGrowth();
        Double avgProcessingTime = calculateAvgProcessingTime();
        Double efficiency = calculateEfficiency();

        return new DashboardSummary(
            pendingDocumentsCount,
            assignedCasesCount,
            unreadCommunicationsCount,
            overdueCasesCount,
            totalDocuments,
            approvedDocuments,
            rejectedDocuments,
            activeCases,
            unreadMessages,
            monthlyGrowth,
            avgProcessingTime,
            efficiency
        );
    }

    public List<DocumentResponse> getPendingDocuments() {
        User currentUser = getCurrentUser();

        // Only CEO, CFO, and ADMIN can approve documents
        if (currentUser.getRole() == UserRole.CEO ||
            currentUser.getRole() == UserRole.CFO ||
            currentUser.getRole() == UserRole.ADMIN) {

            Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "uploadedAt"));
            return documentRepository.findByStatus(DocumentStatus.PENDING, pageable).getContent()
                .stream()
                .map(this::mapDocumentToResponse)
                .collect(Collectors.toList());
        }

        return List.of();
    }

    public List<CaseResponse> getAssignedCases() {
        User currentUser = getCurrentUser();
        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "updatedAt"));

        if (currentUser.getRole() == UserRole.ADMIN ||
            currentUser.getRole() == UserRole.CEO ||
            currentUser.getRole() == UserRole.CFO) {
            // Show all active cases
            return caseRepository.findAll(pageable).getContent().stream()
                .filter(c -> c.getStatus() != CaseStatus.ARCHIVED)
                .map(this::mapCaseToResponse)
                .collect(Collectors.toList());
        } else {
            // Show only assigned cases
            return caseRepository.findByAssignedToId(currentUser.getId(), pageable).getContent()
                .stream()
                .map(this::mapCaseToResponse)
                .collect(Collectors.toList());
        }
    }

    public List<CommunicationResponse> getNotifications() {
        User currentUser = getCurrentUser();
        // Get unread communications for the current user, ordered by most recent first
        return communicationRepository.findByRecipientIdOrderBySentAtDesc(currentUser.getId())
            .stream()
            .map(this::mapCommunicationToResponse)
            .collect(Collectors.toList());
    }

    private long getPendingDocumentsCount(User user) {
        if (user.getRole() == UserRole.CEO || 
            user.getRole() == UserRole.CFO || 
            user.getRole() == UserRole.ADMIN) {
            
            Pageable pageable = PageRequest.of(0, 1);
            return documentRepository.findByStatus(DocumentStatus.PENDING, pageable).getTotalElements();
        }
        return 0;
    }

    private long getAssignedCasesCount(User user) {
        if (user.getRole() == UserRole.ADMIN || 
            user.getRole() == UserRole.CEO || 
            user.getRole() == UserRole.CFO) {
            return caseRepository.findByStatus(CaseStatus.OPEN).size() + 
                   caseRepository.findByStatus(CaseStatus.IN_PROGRESS).size();
        } else {
            Pageable pageable = PageRequest.of(0, 1);
            return caseRepository.findByAssignedToId(user.getId(), pageable).getTotalElements();
        }
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmailIgnoreCase(email)
            .orElseThrow(() -> new EntityNotFoundException("Current user not found"));
    }

    private Double calculateMonthlyGrowth() {
        // Calculate percentage growth of documents processed this month vs last month
        // For now, return a placeholder value
        // TODO: Implement actual monthly growth calculation
        return 0.0;
    }

    private Double calculateAvgProcessingTime() {
        // Calculate average time from upload to approval in hours
        // For now, return a placeholder value
        // TODO: Implement actual average processing time calculation
        return 0.0;
    }

    private Double calculateEfficiency() {
        // Calculate efficiency as percentage of approved documents vs total processed
        long totalProcessed = documentRepository.findByStatus(DocumentStatus.APPROVED, PageRequest.of(0, 1)).getTotalElements() +
                             documentRepository.findByStatus(DocumentStatus.REJECTED, PageRequest.of(0, 1)).getTotalElements();
        if (totalProcessed == 0) {
            return 0.0;
        }
        long approved = documentRepository.findByStatus(DocumentStatus.APPROVED, PageRequest.of(0, 1)).getTotalElements();
        return (double) (approved * 100) / totalProcessed;
    }

    private DocumentResponse mapDocumentToResponse(Document document) {
        Long caseId = document.getCaseRef() != null ? document.getCaseRef().getId() : null;
        String caseTitle = document.getCaseRef() != null ? document.getCaseRef().getTitle() : null;
        Long uploadedById = document.getUploadedBy() != null ? document.getUploadedBy().getId() : null;
        String uploadedByName = document.getUploadedBy() != null ? document.getUploadedBy().getName() : null;
        Long approvedById = document.getApprovedBy() != null ? document.getApprovedBy().getId() : null;
        String approvedByName = document.getApprovedBy() != null ? document.getApprovedBy().getName() : null;

        return new DocumentResponse(
            document.getId(),
            document.getTitle(),
            document.getType(),
            document.getStatus(),
            caseId,
            caseTitle,
            uploadedById,
            uploadedByName,
            approvedById,
            approvedByName,
            document.getFileSize(),
            document.getFilePath(),
            document.getReceiptNumber(),
            document.getUploadedAt(),
            document.getProcessedAt()
        );
    }

    private CaseResponse mapCaseToResponse(Case caseEntity) {
        return new CaseResponse(
            caseEntity.getId(),
            caseEntity.getTitle(),
            caseEntity.getDescription(),
            caseEntity.getStatus(),
            caseEntity.getAssignedTo() != null ? mapToUserSummary(caseEntity.getAssignedTo()) : null,
            mapToUserSummary(caseEntity.getCreatedBy()),
            caseEntity.getCreatedAt(),
            caseEntity.getUpdatedAt()
        );
    }

    private CommunicationResponse mapCommunicationToResponse(Communication communication) {
        return new CommunicationResponse(
            communication.getId(),
            communication.getType(),
            communication.getContent(),
            communication.isRead(),
            mapToUserSummary(communication.getSender()),
            mapToUserSummary(communication.getRecipient()),
            communication.getCaseRef() != null ? communication.getCaseRef().getId() : null,
            communication.getSentAt(),
            communication.getReadAt()
        );
    }

    private UserSummary mapToUserSummary(User user) {
        return new UserSummary(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getRole().toString()
        );
    }
}
