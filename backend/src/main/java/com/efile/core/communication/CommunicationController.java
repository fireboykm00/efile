package com.efile.core.communication;

import com.efile.core.communication.dto.CommunicationRequest;
import com.efile.core.communication.dto.CommunicationResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/communications")
public class CommunicationController {

    private final CommunicationService communicationService;

    public CommunicationController(CommunicationService communicationService) {
        this.communicationService = communicationService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CommunicationResponse>> getCommunications() {
        List<CommunicationResponse> communications = communicationService.getCommunicationsForUser();
        return ResponseEntity.ok(communications);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommunicationResponse> getCommunication(@PathVariable Long id) {
        CommunicationResponse communication = communicationService.getCommunicationById(id);
        return ResponseEntity.ok(communication);
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommunicationResponse> sendCommunication(@Valid @RequestBody CommunicationRequest request) {
        CommunicationResponse communication = communicationService.sendCommunication(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(communication);
    }

    @PutMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        communicationService.markAsRead(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Long> getUnreadCount() {
        long count = communicationService.getUnreadCount();
        return ResponseEntity.ok(count);
    }
}
