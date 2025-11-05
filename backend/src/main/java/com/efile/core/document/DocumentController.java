package com.efile.core.document;

import com.efile.core.common.PageResponse;
import com.efile.core.document.dto.DocumentHistoryResponse;
import com.efile.core.document.dto.DocumentResponse;
import com.efile.core.document.dto.DocumentSearchCriteria;
import com.efile.core.document.dto.DocumentRejectionRequest;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping("/upload")
    @PreAuthorize("hasAnyRole('ADMIN','CEO','CFO','PROCUREMENT','ACCOUNTANT')")
    public ResponseEntity<DocumentResponse> upload(
        @RequestParam("title") String title,
        @RequestParam("type") DocumentType type,
        @RequestParam("caseId") Long caseId,
        @RequestParam("file") MultipartFile file
    ) {
        DocumentResponse response = documentService.uploadDocument(title, type, caseId, file);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping
    public ResponseEntity<PageResponse<DocumentResponse>> search(
        @RequestParam(value = "status", required = false) DocumentStatus status,
        @RequestParam(value = "type", required = false) DocumentType type,
        @RequestParam(value = "uploadedAfter", required = false) Instant uploadedAfter,
        @RequestParam(value = "uploadedBefore", required = false) Instant uploadedBefore,
        @RequestParam(value = "caseId", required = false) Long caseId,
        @RequestParam(value = "title", required = false) String title,
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "uploadedAt"));
        DocumentSearchCriteria criteria = DocumentSearchCriteria.of(status, type, uploadedAfter, uploadedBefore, caseId, title);
        Page<DocumentResponse> result = documentService.searchDocuments(criteria, pageable);
        return ResponseEntity.ok(PageResponse.from(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentResponse> getDocument(@PathVariable Long id) {
        DocumentResponse response = documentService.getDocument(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<DocumentHistoryResponse>> history(@PathVariable Long id) {
        List<DocumentHistoryResponse> history = documentService.getHistory(id);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        DocumentResponse document = documentService.getDocument(id);
        Resource resource = documentService.download(id);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + document.filePath().substring(document.filePath().lastIndexOf('/') + 1))
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(resource);
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN','CEO','CFO')")
    public ResponseEntity<DocumentResponse> approve(@PathVariable Long id) {
        DocumentResponse response = documentService.approveDocument(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN','CEO','CFO')")
    public ResponseEntity<DocumentResponse> reject(@PathVariable Long id, @Valid @RequestBody DocumentRejectionRequest request) {
        DocumentResponse response = documentService.rejectDocument(id, request.reason());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/submit")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DocumentResponse> submit(@PathVariable Long id) {
        DocumentResponse response = documentService.submitDocument(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/start-review")
    @PreAuthorize("hasAnyRole('ADMIN','CEO','CFO','AUDITOR')")
    public ResponseEntity<DocumentResponse> startReview(@PathVariable Long id) {
        DocumentResponse response = documentService.startReview(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/withdraw")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DocumentResponse> withdraw(@PathVariable Long id) {
        DocumentResponse response = documentService.withdrawDocument(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/receipt")
    public ResponseEntity<String> getReceipt(@PathVariable Long id) {
        String receipt = documentService.generateReceiptDocument(id);
        String filename = "receipt_" + id + ".txt";
        
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
            .contentType(MediaType.TEXT_PLAIN)
            .body(receipt);
    }
}
