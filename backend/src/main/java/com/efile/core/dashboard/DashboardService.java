package com.efile.core.dashboard;

import com.efile.core.casemanagement.Case;
import com.efile.core.casemanagement.CaseRepository;
import com.efile.core.casemanagement.CaseStatus;
import com.efile.core.communication.CommunicationRepository;
import com.efile.core.dashboard.dto.DashboardSummary;
import com.efile.core.document.Document;
import com.efile.core.document.DocumentRepository;
import com.efile.core.document.DocumentStatus;
import com.efile.core.user.User;
import com.efile.core.user.UserRepository;
import com.efile.core.user.UserRole;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        
        long pendingDocuments = getPendingDocumentsCount(currentUser);
        long assignedCases = getAssignedCasesCount(currentUser);
        long unreadCommunications = communicationRepository.countByRecipientAndIsRead(currentUser, false);
        
        return new DashboardSummary(
            pendingDocuments,
            assignedCases,
            unreadCommunications
        );
    }

    public List<Document> getPendingDocuments() {
        User currentUser = getCurrentUser();
        
        // Only CEO, CFO, and ADMIN can approve documents
        if (currentUser.getRole() == UserRole.CEO || 
            currentUser.getRole() == UserRole.CFO || 
            currentUser.getRole() == UserRole.ADMIN) {
            
            Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "uploadedAt"));
            return documentRepository.findByStatus(DocumentStatus.PENDING, pageable).getContent();
        }
        
        return List.of();
    }

    public List<Case> getAssignedCases() {
        User currentUser = getCurrentUser();
        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "updatedAt"));
        
        if (currentUser.getRole() == UserRole.ADMIN || 
            currentUser.getRole() == UserRole.CEO || 
            currentUser.getRole() == UserRole.CFO) {
            // Show all active cases
            return caseRepository.findAll(pageable).getContent().stream()
                .filter(c -> c.getStatus() != CaseStatus.ARCHIVED)
                .collect(Collectors.toList());
        } else {
            // Show only assigned cases
            return caseRepository.findByAssignedToId(currentUser.getId(), pageable).getContent();
        }
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
}
