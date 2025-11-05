package com.efile.core.casemanagement;

import com.efile.core.casemanagement.dto.CaseRequest;
import com.efile.core.casemanagement.dto.CaseResponse;
import com.efile.core.document.Document;
import com.efile.core.document.DocumentRepository;
import com.efile.core.document.DocumentService;
import com.efile.core.document.dto.DocumentResponse;
import com.efile.core.user.User;
import com.efile.core.user.UserRepository;
import com.efile.core.user.UserRole;
import com.efile.core.user.dto.UserSummary;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CaseService {

    private final CaseRepository caseRepository;
    private final UserRepository userRepository;
    private final DocumentRepository documentRepository;
    private final DocumentService documentService;
    private final ObjectMapper objectMapper;

    public CaseService(CaseRepository caseRepository, UserRepository userRepository, 
                      DocumentRepository documentRepository, DocumentService documentService, ObjectMapper objectMapper) {
        this.caseRepository = caseRepository;
        this.userRepository = userRepository;
        this.documentRepository = documentRepository;
        this.documentService = documentService;
        this.objectMapper = objectMapper;
    }

    public CaseResponse createCase(CaseRequest request) {
        User currentUser = getCurrentUser();
        
        Case caseEntity = new Case();
        caseEntity.setTitle(request.title());
        caseEntity.setDescription(request.description());
        caseEntity.setStatus(request.status() != null ? request.status() : CaseStatus.OPEN);
        caseEntity.setPriority(request.priority() != null ? request.priority() : CasePriority.MEDIUM);
        caseEntity.setCategory(request.category() != null ? request.category() : CaseCategory.GENERAL);
        caseEntity.setCreatedBy(currentUser);

        // Handle tags - store as JSON string
        if (request.tags() != null && !request.tags().isEmpty()) {
            try {
                caseEntity.setTags(objectMapper.writeValueAsString(request.tags()));
            } catch (Exception e) {
                // Handle JSON serialization error
                caseEntity.setTags("[]");
            }
        }

        // Handle date fields
        if (request.dueDate() != null && !request.dueDate().isEmpty()) {
            try {
                caseEntity.setDueDate(Instant.parse(request.dueDate()));
            } catch (Exception e) {
                // Handle date parsing error
                caseEntity.setDueDate(null);
            }
        }

        if (request.estimatedCompletionDate() != null && !request.estimatedCompletionDate().isEmpty()) {
            try {
                caseEntity.setEstimatedCompletionDate(Instant.parse(request.estimatedCompletionDate()));
            } catch (Exception e) {
                // Handle date parsing error
                caseEntity.setEstimatedCompletionDate(null);
            }
        }

        caseEntity.setBudget(request.budget());
        caseEntity.setLocation(request.location());
        caseEntity.setDepartment(request.department());

        if (request.assignedToId() != null) {
            Long assignedId = request.assignedToId();
            User assignedUser = userRepository.findById(assignedId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + assignedId));
            caseEntity.setAssignedTo(assignedUser);
        }

        Case saved = caseRepository.save(caseEntity);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public CaseResponse getCaseById(Long id) {
        Case caseEntity = caseRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Case not found with id: " + id));
        
        // Check access
        checkCaseAccess(caseEntity);
        
        return mapToResponse(caseEntity);
    }

    @Transactional(readOnly = true)
    public List<CaseResponse> getAllCases() {
        User currentUser = getCurrentUser();
        List<Case> cases;

        // Role-based filtering
        if (hasRole(currentUser, UserRole.ADMIN, UserRole.CEO, UserRole.CFO, UserRole.AUDITOR)) {
            cases = caseRepository.findAll();
        } else {
            // Users can see cases they created or are assigned to
            cases = caseRepository.findByCreatedByOrAssignedTo(currentUser, currentUser);
        }

        return cases.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public CaseResponse updateCase(Long id, CaseRequest request) {
        Case caseEntity = caseRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Case not found with id: " + id));

        checkCaseAccess(caseEntity);

        caseEntity.setTitle(request.title());
        caseEntity.setDescription(request.description());
        
        if (request.status() != null) {
            caseEntity.setStatus(request.status());
        }

        if (request.assignedToId() != null) {
            Long assignedId = request.assignedToId();
            User assignedUser = userRepository.findById(assignedId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + assignedId));
            caseEntity.setAssignedTo(assignedUser);
        }

        Case updated = caseRepository.save(caseEntity);
        return mapToResponse(updated);
    }

    public CaseResponse assignCase(Long caseId, Long userId) {
        Case caseEntity = caseRepository.findById(caseId)
            .orElseThrow(() -> new EntityNotFoundException("Case not found with id: " + caseId));

        User assignedUser = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        caseEntity.setAssignedTo(assignedUser);
        Case updated = caseRepository.save(caseEntity);
        return mapToResponse(updated);
    }

    public void archiveCase(Long id) {
        Case caseEntity = caseRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Case not found with id: " + id));

        checkCaseAccess(caseEntity);
        caseEntity.setStatus(CaseStatus.CLOSED);
        caseRepository.save(caseEntity);
    }

    @Transactional(readOnly = true)
    public CaseResponse getCaseWithDocuments(Long id) {
        Case caseEntity = caseRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Case not found with id: " + id));
        
        checkCaseAccess(caseEntity);
        
        List<Document> documents = documentRepository.findByCaseRef(caseEntity);
        List<DocumentResponse> documentResponses = documents.stream()
            .map(documentService::toResponse)
            .collect(Collectors.toList());
        
        return mapToResponseWithDocuments(caseEntity, documentResponses);
    }

    public CaseResponse updateCaseStatus(Long id, CaseStatus newStatus) {
        Case caseEntity = caseRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Case not found with id: " + id));

        checkCaseAccess(caseEntity);
        validateCaseStatusTransition(caseEntity.getStatus(), newStatus);
        
        caseEntity.setStatus(newStatus);
        Case updated = caseRepository.save(caseEntity);
        return mapToResponseWithDocuments(updated, getDocumentsForCase(updated.getId()));
    }

    private List<DocumentResponse> getDocumentsForCase(Long caseId) {
        List<Document> documents = documentRepository.findByCaseRefId(caseId);
        return documents.stream()
            .map(documentService::toResponse)
            .collect(Collectors.toList());
    }

    private void validateCaseStatusTransition(CaseStatus currentStatus, CaseStatus newStatus) {
        switch (currentStatus) {
            case OPEN:
                if (newStatus != CaseStatus.ACTIVE && newStatus != CaseStatus.CLOSED) {
                    throw new IllegalStateException("Open cases can only be activated or closed");
                }
                break;
            case ACTIVE:
                if (newStatus != CaseStatus.UNDER_REVIEW && newStatus != CaseStatus.ON_HOLD && newStatus != CaseStatus.CLOSED) {
                    throw new IllegalStateException("Active cases can only be moved to review, on hold, or closed");
                }
                break;
            case UNDER_REVIEW:
                if (newStatus != CaseStatus.COMPLETED && newStatus != CaseStatus.ACTIVE && newStatus != CaseStatus.CLOSED) {
                    throw new IllegalStateException("Cases under review can only be completed, returned to active, or closed");
                }
                break;
            case ON_HOLD:
                if (newStatus != CaseStatus.ACTIVE && newStatus != CaseStatus.CLOSED) {
                    throw new IllegalStateException("Cases on hold can only be reactivated or closed");
                }
                break;
            case COMPLETED:
                if (newStatus != CaseStatus.CLOSED) {
                    throw new IllegalStateException("Completed cases can only be closed");
                }
                break;
            case CLOSED:
                throw new IllegalStateException("Closed cases cannot be changed");
        }
    }

    private CaseResponse mapToResponse(Case caseEntity) {
        return mapToResponseWithDocuments(caseEntity, getDocumentsForCase(caseEntity.getId()));
    }

    private CaseResponse mapToResponseWithDocuments(Case caseEntity, List<DocumentResponse> documents) {
        // Parse tags from JSON string
        List<String> tags = List.of();
        if (caseEntity.getTags() != null && !caseEntity.getTags().isEmpty()) {
            try {
                tags = objectMapper.readValue(caseEntity.getTags(), List.class);
            } catch (Exception e) {
                tags = List.of();
            }
        }

        return new CaseResponse(
            caseEntity.getId(),
            caseEntity.getTitle(),
            caseEntity.getDescription(),
            caseEntity.getStatus(),
            caseEntity.getPriority(),
            caseEntity.getCategory(),
            tags,
            caseEntity.getDueDate(),
            caseEntity.getEstimatedCompletionDate(),
            caseEntity.getBudget(),
            caseEntity.getLocation(),
            caseEntity.getDepartment(),
            caseEntity.getAssignedTo() != null ? mapToUserSummary(caseEntity.getAssignedTo()) : null,
            mapToUserSummary(caseEntity.getCreatedBy()),
            caseEntity.getCreatedAt(),
            caseEntity.getUpdatedAt(),
            documents
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

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmailIgnoreCase(email)
            .orElseThrow(() -> new EntityNotFoundException("Current user not found"));
    }

    private void checkCaseAccess(Case caseEntity) {
        User currentUser = getCurrentUser();
        
        // ADMIN, CEO, CFO, AUDITOR can access all cases
        if (hasRole(currentUser, UserRole.ADMIN, UserRole.CEO, UserRole.CFO, UserRole.AUDITOR)) {
            return;
        }

        // Others can only access cases they created or are assigned to
        if (!caseEntity.getCreatedBy().equals(currentUser) && 
            (caseEntity.getAssignedTo() == null || !caseEntity.getAssignedTo().equals(currentUser))) {
            throw new AccessDeniedException("You don't have access to this case");
        }
    }

    private boolean hasRole(User user, UserRole... roles) {
        for (UserRole role : roles) {
            if (user.getRole() == role) {
                return true;
            }
        }
        return false;
    }
}
