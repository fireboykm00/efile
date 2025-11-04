package com.efile.core.communication;

import com.efile.core.casemanagement.Case;
import com.efile.core.casemanagement.CaseRepository;
import com.efile.core.communication.dto.CommunicationRequest;
import com.efile.core.communication.dto.CommunicationResponse;
import com.efile.core.security.EncryptionService;
import com.efile.core.user.User;
import com.efile.core.user.UserRepository;
import com.efile.core.user.UserRole;
import com.efile.core.user.dto.UserSummary;
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
public class CommunicationService {

    private final CommunicationRepository communicationRepository;
    private final UserRepository userRepository;
    private final CaseRepository caseRepository;
    private final EncryptionService encryptionService;

    public CommunicationService(
        CommunicationRepository communicationRepository,
        UserRepository userRepository,
        CaseRepository caseRepository,
        EncryptionService encryptionService
    ) {
        this.communicationRepository = communicationRepository;
        this.userRepository = userRepository;
        this.caseRepository = caseRepository;
        this.encryptionService = encryptionService;
    }

    public CommunicationResponse sendCommunication(CommunicationRequest request) {
        User sender = getCurrentUser();
        
        User recipient = userRepository.findById(request.recipientId())
            .orElseThrow(() -> new EntityNotFoundException("Recipient not found with id: " + request.recipientId()));

        Case caseEntity = caseRepository.findById(request.caseId())
            .orElseThrow(() -> new EntityNotFoundException("Case not found with id: " + request.caseId()));

        // Validate recipient based on sender role (INVESTOR restrictions)
        validateRecipient(sender, recipient);

        Communication communication = new Communication();
        communication.setType(request.type());
        communication.setContent(encryptionService.encrypt(request.content()));
        communication.setSender(sender);
        communication.setRecipient(recipient);
        communication.setCaseRef(caseEntity);
        communication.setRead(false);

        Communication saved = communicationRepository.save(communication);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public CommunicationResponse getCommunicationById(Long id) {
        Communication communication = communicationRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Communication not found with id: " + id));

        // Check access - only sender or recipient can view
        User currentUser = getCurrentUser();
        if (!communication.getSender().equals(currentUser) && !communication.getRecipient().equals(currentUser)) {
            throw new AccessDeniedException("You don't have access to this communication");
        }

        return mapToResponse(communication);
    }

    @Transactional(readOnly = true)
    public List<CommunicationResponse> getCommunicationsForUser() {
        User currentUser = getCurrentUser();
        List<Communication> communications = communicationRepository.findBySenderOrRecipient(currentUser, currentUser);
        return communications.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CommunicationResponse> getCommunicationsByCase(Long caseId) {
        Case caseEntity = caseRepository.findById(caseId)
            .orElseThrow(() -> new EntityNotFoundException("Case not found with id: " + caseId));

        List<Communication> communications = communicationRepository.findByCaseRef(caseEntity);
        return communications.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public void markAsRead(Long id) {
        Communication communication = communicationRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Communication not found with id: " + id));

        User currentUser = getCurrentUser();
        if (!communication.getRecipient().equals(currentUser)) {
            throw new AccessDeniedException("Only the recipient can mark this as read");
        }

        if (!communication.isRead()) {
            communication.setRead(true);
            communication.setReadAt(Instant.now());
            communicationRepository.save(communication);
        }
    }

    @Transactional(readOnly = true)
    public long getUnreadCount() {
        User currentUser = getCurrentUser();
        return communicationRepository.countByRecipientAndIsRead(currentUser, false);
    }

    private void validateRecipient(User sender, User recipient) {
        // INVESTOR can only send to ADMIN, CEO, or CFO
        if (sender.getRole() == UserRole.INVESTOR) {
            if (recipient.getRole() != UserRole.ADMIN && 
                recipient.getRole() != UserRole.CEO && 
                recipient.getRole() != UserRole.CFO) {
                throw new AccessDeniedException("Investors can only communicate with ADMIN, CEO, or CFO");
            }
        }
    }

    private CommunicationResponse mapToResponse(Communication communication) {
        return new CommunicationResponse(
            communication.getId(),
            communication.getType(),
            encryptionService.decrypt(communication.getContent()),
            communication.isRead(),
            mapToUserSummary(communication.getSender()),
            mapToUserSummary(communication.getRecipient()),
            communication.getCaseRef().getId(),
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

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmailIgnoreCase(email)
            .orElseThrow(() -> new EntityNotFoundException("Current user not found"));
    }
}
