package com.efile.core.casemanagement;

import com.efile.core.casemanagement.dto.CaseRequest;
import com.efile.core.casemanagement.dto.CaseResponse;
import com.efile.core.user.User;
import com.efile.core.user.UserRepository;
import com.efile.core.user.UserRole;
import com.efile.core.user.dto.UserSummary;
import jakarta.persistence.EntityNotFoundException;
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

    public CaseService(CaseRepository caseRepository, UserRepository userRepository) {
        this.caseRepository = caseRepository;
        this.userRepository = userRepository;
    }

    public CaseResponse createCase(CaseRequest request) {
        User currentUser = getCurrentUser();
        
        Case caseEntity = new Case();
        caseEntity.setTitle(request.title());
        caseEntity.setDescription(request.description());
        caseEntity.setStatus(request.status() != null ? request.status() : CaseStatus.OPEN);
        caseEntity.setCreatedBy(currentUser);

        if (request.assignedToId() != null) {
            User assignedUser = userRepository.findById(request.assignedToId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + request.assignedToId()));
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
            User assignedUser = userRepository.findById(request.assignedToId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + request.assignedToId()));
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
        caseEntity.setStatus(CaseStatus.ARCHIVED);
        caseRepository.save(caseEntity);
    }

    private CaseResponse mapToResponse(Case caseEntity) {
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
