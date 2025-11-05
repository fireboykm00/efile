package com.efile.core.dashboard;

import java.time.Instant;
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

        // Basic counts - role-specific
        long pendingDocumentsCount = getPendingDocumentsCount(currentUser);
        long assignedCasesCount = getAssignedCasesCount(currentUser);
        long unreadCommunicationsCount = communicationRepository.countByRecipientAndIsRead(currentUser, false);
        long overdueCasesCount = calculateOverdueCases(currentUser);

        // Extended metrics
        long totalDocuments = getTotalDocumentsForRole(currentUser);
        long approvedDocuments = getApprovedDocumentsForRole(currentUser);
        long rejectedDocuments = getRejectedDocumentsForRole(currentUser);
        long activeCases = getActiveCasesForRole(currentUser);
        long unreadMessages = unreadCommunicationsCount;

        // Executive metrics (calculated)
        Double monthlyGrowth = calculateMonthlyGrowth();
        Double avgProcessingTime = calculateAvgProcessingTime();
        Double efficiency = calculateEfficiency();

        // Admin-only metrics
        Long totalUsers = (currentUser.getRole() == UserRole.ADMIN) ? getTotalUsers() : null;

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
            efficiency,
            totalUsers
        );
    }

    public List<DocumentResponse> getPendingDocuments() {
        User currentUser = getCurrentUser();

        // Only CEO, CFO, and ADMIN can approve documents
        if (currentUser.getRole() == UserRole.CEO ||
            currentUser.getRole() == UserRole.CFO ||
            currentUser.getRole() == UserRole.ADMIN) {

            Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "uploadedAt"));
            return documentRepository.findByStatus(DocumentStatus.SUBMITTED, pageable).getContent()
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
                .filter(c -> c.getStatus() != CaseStatus.CLOSED)
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

    public Long getTotalUsers() {
        return userRepository.count();
    }

    private long getPendingDocumentsCount(User user) {
        if (user.getRole() == UserRole.CEO || 
            user.getRole() == UserRole.CFO || 
            user.getRole() == UserRole.ADMIN) {
            
            Pageable pageable = PageRequest.of(0, 1);
            return documentRepository.findByStatus(DocumentStatus.SUBMITTED, pageable).getTotalElements();
        }
        return 0;
    }

    private long getAssignedCasesCount(User user) {
        if (user.getRole() == UserRole.ADMIN || 
            user.getRole() == UserRole.CEO || 
            user.getRole() == UserRole.CFO) {
            return caseRepository.findByStatus(CaseStatus.OPEN).size() + 
                   caseRepository.findByStatus(CaseStatus.ACTIVE).size();
        } else {
            Pageable pageable = PageRequest.of(0, 1);
            return caseRepository.findByAssignedToId(user.getId(), pageable).getTotalElements();
        }
    }

    private long calculateOverdueCases(User user) {
        // Calculate cases older than 7 days that are still open/in_progress
        Instant sevenDaysAgo = Instant.now().minus(7, java.time.temporal.ChronoUnit.DAYS);
        
        if (user.getRole() == UserRole.ADMIN || 
            user.getRole() == UserRole.CEO || 
            user.getRole() == UserRole.CFO) {
            return caseRepository.findByStatusAndCreatedAtBefore(CaseStatus.OPEN, sevenDaysAgo).size() +
                   caseRepository.findByStatusAndCreatedAtBefore(CaseStatus.ACTIVE, sevenDaysAgo).size();
        } else {
            return caseRepository.findByAssignedToIdAndStatusAndCreatedAtBefore(
                user.getId(), CaseStatus.OPEN, sevenDaysAgo
            ).size() + caseRepository.findByAssignedToIdAndStatusAndCreatedAtBefore(
                user.getId(), CaseStatus.ACTIVE, sevenDaysAgo
            ).size();
        }
    }

    private long getTotalDocumentsForRole(User user) {
        if (user.getRole() == UserRole.ADMIN || 
            user.getRole() == UserRole.CEO || 
            user.getRole() == UserRole.AUDITOR) {
            return documentRepository.count();
        } else if (user.getRole() == UserRole.CFO) {
            // Financial documents only
            return documentRepository.findByTypeContaining("financial", PageRequest.of(0, 1)).getTotalElements();
        } else if (user.getRole() == UserRole.PROCUREMENT) {
            // Procurement documents only
            return documentRepository.findByTypeContaining("procurement", PageRequest.of(0, 1)).getTotalElements();
        } else if (user.getRole() == UserRole.ACCOUNTANT) {
            // Accounting documents only
            return documentRepository.findByTypeContaining("accounting", PageRequest.of(0, 1)).getTotalElements();
        } else {
            // Investors and others see only approved documents
            return documentRepository.findByStatus(DocumentStatus.APPROVED, PageRequest.of(0, 1)).getTotalElements();
        }
    }

    private long getApprovedDocumentsForRole(User user) {
        if (user.getRole() == UserRole.ADMIN || 
            user.getRole() == UserRole.CEO || 
            user.getRole() == UserRole.AUDITOR) {
            return documentRepository.findByStatus(DocumentStatus.APPROVED, PageRequest.of(0, 1)).getTotalElements();
        } else {
            // Other roles see approved documents in their domain
            return documentRepository.findByStatus(DocumentStatus.APPROVED, PageRequest.of(0, 1)).getTotalElements();
        }
    }

    private long getRejectedDocumentsForRole(User user) {
        if (user.getRole() == UserRole.ADMIN || 
            user.getRole() == UserRole.CEO || 
            user.getRole() == UserRole.AUDITOR) {
            return documentRepository.findByStatus(DocumentStatus.REJECTED, PageRequest.of(0, 1)).getTotalElements();
        } else {
            // Other roles see fewer rejected documents
            return documentRepository.findByStatus(DocumentStatus.REJECTED, PageRequest.of(0, 1)).getTotalElements();
        }
    }

    private long getActiveCasesForRole(User user) {
        if (user.getRole() == UserRole.ADMIN || 
            user.getRole() == UserRole.CEO || 
            user.getRole() == UserRole.CFO) {
            return caseRepository.findByStatus(CaseStatus.OPEN).size() + 
                   caseRepository.findByStatus(CaseStatus.ACTIVE).size();
        } else {
            Pageable pageable = PageRequest.of(0, 1);
            return caseRepository.findByAssignedToIdAndStatus(user.getId(), CaseStatus.OPEN, pageable).getTotalElements() +
                   caseRepository.findByAssignedToIdAndStatus(user.getId(), CaseStatus.ACTIVE, pageable).getTotalElements();
        }
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmailIgnoreCase(email)
            .orElseThrow(() -> new EntityNotFoundException("Current user not found"));
    }

    private Double calculateMonthlyGrowth() {
        try {
            Instant oneMonthAgo = Instant.now().minus(30, java.time.temporal.ChronoUnit.DAYS);
            Instant twoMonthsAgo = Instant.now().minus(60, java.time.temporal.ChronoUnit.DAYS);
            
            long currentMonthDocs = documentRepository.findByUploadedAtAfter(oneMonthAgo).size();
            long previousMonthDocs = documentRepository.findByUploadedAtBetween(twoMonthsAgo, oneMonthAgo).size();
            
            if (previousMonthDocs == 0) return 0.0;
            return ((double)(currentMonthDocs - previousMonthDocs) / previousMonthDocs) * 100;
        } catch (Exception e) {
            return 0.0;
        }
    }

    private Double calculateAvgProcessingTime() {
        try {
            List<Document> processedDocs = documentRepository.findByStatus(DocumentStatus.APPROVED, PageRequest.of(0, Integer.MAX_VALUE)).getContent();
            if (processedDocs.isEmpty()) return 0.0;
            
            long totalMinutes = processedDocs.stream()
                .filter(doc -> doc.getProcessedAt() != null)
                .mapToLong(doc -> java.time.Duration.between(doc.getUploadedAt(), doc.getProcessedAt()).toMinutes())
                .sum();
            
            return processedDocs.isEmpty() ? 0.0 : (double) totalMinutes / processedDocs.size() / 60; // Convert to hours
        } catch (Exception e) {
            return 0.0;
        }
    }

    private Double calculateEfficiency() {
        try {
            long totalDocs = documentRepository.count();
            if (totalDocs == 0) return 100.0;
            
            long approvedDocs = documentRepository.findByStatus(DocumentStatus.APPROVED, PageRequest.of(0, Integer.MAX_VALUE)).getTotalElements();
            long pendingDocs = documentRepository.findByStatus(DocumentStatus.SUBMITTED, PageRequest.of(0, Integer.MAX_VALUE)).getTotalElements();
            
            // Efficiency = (Approved / Total) * 100 - (Pending / Total) * 10 penalty
            double efficiency = ((double) approvedDocs / totalDocs) * 100;
            double pendingPenalty = ((double) pendingDocs / totalDocs) * 10;
            
            return Math.max(0, Math.min(100, efficiency - pendingPenalty));
        } catch (Exception e) {
            return 0.0;
        }
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
            caseEntity.getPriority(),
            caseEntity.getCategory(),
            List.of(), // Empty tags list for dashboard
            caseEntity.getDueDate(),
            caseEntity.getEstimatedCompletionDate(),
            caseEntity.getBudget(),
            caseEntity.getLocation(),
            caseEntity.getDepartment(),
            caseEntity.getAssignedTo() != null ? mapToUserSummary(caseEntity.getAssignedTo()) : null,
            mapToUserSummary(caseEntity.getCreatedBy()),
            caseEntity.getCreatedAt(),
            caseEntity.getUpdatedAt(),
            List.of() // Empty documents list for dashboard
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
