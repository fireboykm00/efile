package com.efile.core.casemanagement;

import com.efile.core.casemanagement.dto.AssignCaseRequest;
import com.efile.core.casemanagement.dto.CaseRequest;
import com.efile.core.casemanagement.dto.CaseResponse;
import com.efile.core.communication.dto.CommunicationResponse;
import com.efile.core.communication.CommunicationService;
import com.efile.core.document.dto.DocumentResponse;
import com.efile.core.document.DocumentService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cases")
public class CaseController {

    private final CaseService caseService;
    private final DocumentService documentService;
    private final CommunicationService communicationService;

    public CaseController(
        CaseService caseService,
        DocumentService documentService,
        CommunicationService communicationService
    ) {
        this.caseService = caseService;
        this.documentService = documentService;
        this.communicationService = communicationService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CaseResponse>> getCases() {
        List<CaseResponse> cases = caseService.getAllCases();
        return ResponseEntity.ok(cases);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CaseResponse> getCase(@PathVariable Long id) {
        CaseResponse caseResponse = caseService.getCaseById(id);
        return ResponseEntity.ok(caseResponse);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','CEO','CFO','PROCUREMENT','ACCOUNTANT')")
    public ResponseEntity<CaseResponse> createCase(@Valid @RequestBody CaseRequest request) {
        CaseResponse caseResponse = caseService.createCase(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(caseResponse);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CaseResponse> updateCase(
        @PathVariable Long id,
        @Valid @RequestBody CaseRequest request
    ) {
        CaseResponse caseResponse = caseService.updateCase(id, request);
        return ResponseEntity.ok(caseResponse);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CEO','CFO')")
    public ResponseEntity<Void> archiveCase(@PathVariable Long id) {
        caseService.archiveCase(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/documents")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<DocumentResponse>> getCaseDocuments(@PathVariable Long id) {
        List<DocumentResponse> documents = documentService.getDocumentsByCase(id);
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/{id}/communications")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CommunicationResponse>> getCaseCommunications(@PathVariable Long id) {
        List<CommunicationResponse> communications = communicationService.getCommunicationsByCase(id);
        return ResponseEntity.ok(communications);
    }

    @PutMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('ADMIN','CEO','CFO')")
    public ResponseEntity<CaseResponse> assignCase(
        @PathVariable Long id,
        @Valid @RequestBody AssignCaseRequest request
    ) {
        CaseResponse caseResponse = caseService.assignCase(id, request.userId());
        return ResponseEntity.ok(caseResponse);
    }
}
