package com.efile.core.document;

import com.efile.core.casemanagement.Case;
import com.efile.core.casemanagement.CaseRepository;
import com.efile.core.document.dto.DocumentHistoryResponse;
import com.efile.core.document.dto.DocumentResponse;
import com.efile.core.document.dto.DocumentSearchCriteria;
import com.efile.core.storage.FileStorageService;
import com.efile.core.user.User;
import com.efile.core.user.UserRepository;
import com.efile.core.user.UserRole;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentStatusHistoryRepository historyRepository;
    private final CaseRepository caseRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    public DocumentService(
        DocumentRepository documentRepository,
        DocumentStatusHistoryRepository historyRepository,
        CaseRepository caseRepository,
        UserRepository userRepository,
        FileStorageService fileStorageService
    ) {
        this.documentRepository = documentRepository;
        this.historyRepository = historyRepository;
        this.caseRepository = caseRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
    }

    @Transactional
    public DocumentResponse uploadDocument(String title, DocumentType type, Long caseId, MultipartFile file) {
        if (!StringUtils.hasText(title)) {
            throw new IllegalArgumentException("Title is required");
        }
        if (type == null) {
            throw new IllegalArgumentException("Document type is required");
        }
        if (caseId == null) {
            throw new IllegalArgumentException("Case is required");
        }
        Case caseEntity = caseRepository.findById(caseId).orElseThrow(() -> new IllegalArgumentException("Case not found"));
        User uploader = currentUser();
        String storedPath = fileStorageService.store(file, caseId);
        Document document = new Document();
        document.setTitle(title);
        document.setType(type);
        document.setFilePath(storedPath);
        document.setFileSize(file.getSize());
        document.setStatus(DocumentStatus.PENDING);
        document.setCaseRef(caseEntity);
        document.setUploadedBy(uploader);
        document.setReceiptNumber(generateReceiptNumber());
        Document saved = documentRepository.save(document);
        recordHistory(saved, DocumentStatus.PENDING, "Document uploaded");
        return toResponse(saved);
    }

    @Transactional
    public DocumentResponse approveDocument(Long documentId) {
        Document document = documentRepository.findById(documentId).orElseThrow(() -> new IllegalArgumentException("Document not found"));
        if (document.getStatus() == DocumentStatus.APPROVED) {
            return toResponse(document);
        }
        User approver = currentUser();
        ensureApproverRole(approver.getRole());
        document.setStatus(DocumentStatus.APPROVED);
        document.setApprovedBy(approver);
        document.setProcessedAt(Instant.now());
        document.setRejectionReason(null);
        Document saved = documentRepository.save(document);
        recordHistory(saved, DocumentStatus.APPROVED, "Approved by " + approver.getName());
        return toResponse(saved);
    }

    @Transactional
    public DocumentResponse rejectDocument(Long documentId, String reason) {
        if (!StringUtils.hasText(reason) || reason.length() < 10) {
            throw new IllegalArgumentException("Rejection reason must be at least 10 characters");
        }
        Document document = documentRepository.findById(documentId).orElseThrow(() -> new IllegalArgumentException("Document not found"));
        User approver = currentUser();
        ensureApproverRole(approver.getRole());
        document.setStatus(DocumentStatus.REJECTED);
        document.setApprovedBy(approver);
        document.setProcessedAt(Instant.now());
        document.setRejectionReason(reason);
        Document saved = documentRepository.save(document);
        recordHistory(saved, DocumentStatus.REJECTED, reason);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<DocumentResponse> searchDocuments(DocumentSearchCriteria criteria, Pageable pageable) {
        Specification<Document> specification = Specification.where(null);
        if (criteria.status().isPresent()) {
            specification = specification.and((root, query, cb) -> cb.equal(root.get("status"), criteria.status().get()));
        }
        if (criteria.type().isPresent()) {
            specification = specification.and((root, query, cb) -> cb.equal(root.get("type"), criteria.type().get()));
        }
        if (criteria.caseId().isPresent()) {
            specification = specification.and((root, query, cb) -> cb.equal(root.get("caseRef").get("id"), criteria.caseId().get()));
        }
        if (criteria.uploadedAfter().isPresent()) {
            specification = specification.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("uploadedAt"), criteria.uploadedAfter().get()));
        }
        if (criteria.uploadedBefore().isPresent()) {
            specification = specification.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("uploadedAt"), criteria.uploadedBefore().get()));
        }
        if (criteria.titleKeyword().isPresent()) {
            String pattern = "%" + criteria.titleKeyword().get().toLowerCase() + "%";
            specification = specification.and((root, query, cb) -> cb.like(cb.lower(root.get("title")), pattern));
        }
        return documentRepository.findAll(specification, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public DocumentResponse getDocument(Long documentId) {
        Document document = documentRepository.findById(documentId).orElseThrow(() -> new IllegalArgumentException("Document not found"));
        return toResponse(document);
    }

    @Transactional(readOnly = true)
    public List<DocumentHistoryResponse> getHistory(Long documentId) {
        documentRepository.findById(documentId).orElseThrow(() -> new IllegalArgumentException("Document not found"));
        return historyRepository
            .findByDocumentIdOrderByChangedAtAsc(documentId)
            .stream()
            .map(history -> new DocumentHistoryResponse(DocumentStatus.valueOf(history.getStatus()), history.getComment(), history.getChangedAt()))
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Resource download(Long documentId) {
        Document document = documentRepository.findById(documentId).orElseThrow(() -> new IllegalArgumentException("Document not found"));
        return fileStorageService.loadAsResource(document.getFilePath());
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> getDocumentsByCase(Long caseId) {
        Case caseEntity = caseRepository.findById(caseId)
            .orElseThrow(() -> new IllegalArgumentException("Case not found with id: " + caseId));
        List<Document> documents = documentRepository.findByCaseRef(caseEntity);
        return documents.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public void delete(Long documentId) {
        Document document = documentRepository.findById(documentId).orElseThrow(() -> new IllegalArgumentException("Document not found"));
        historyRepository.deleteByDocumentId(documentId);
        documentRepository.delete(document);
        fileStorageService.delete(document.getFilePath());
    }

    private void ensureApproverRole(UserRole role) {
        if (!(role == UserRole.CEO || role == UserRole.CFO || role == UserRole.ADMIN)) {
            throw new IllegalArgumentException("User not authorized to approve documents");
        }
    }

    private DocumentResponse toResponse(Document document) {
        Long caseId = Optional.ofNullable(document.getCaseRef()).map(Case::getId).orElse(null);
        String caseTitle = Optional.ofNullable(document.getCaseRef()).map(Case::getTitle).orElse(null);
        Long uploadedById = Optional.ofNullable(document.getUploadedBy()).map(User::getId).orElse(null);
        String uploadedByName = Optional.ofNullable(document.getUploadedBy()).map(User::getName).orElse(null);
        Long approvedById = Optional.ofNullable(document.getApprovedBy()).map(User::getId).orElse(null);
        String approvedByName = Optional.ofNullable(document.getApprovedBy()).map(User::getName).orElse(null);
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

    private void recordHistory(Document document, DocumentStatus status, String comment) {
        DocumentStatusHistory history = new DocumentStatusHistory();
        history.setDocument(document);
        history.setStatus(status.name());
        history.setComment(comment);
        historyRepository.save(history);
    }

    private User currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof com.efile.core.security.UserPrincipal principal)) {
            throw new IllegalStateException("No authenticated user");
        }
        return userRepository.findById(principal.getId()).orElseThrow(() -> new IllegalStateException("User not found"));
    }

    private String generateReceiptNumber() {
        String candidate;
        do {
            candidate = UUID.randomUUID().toString().replace("-", "").substring(0, 20).toUpperCase();
        } while (documentRepository.findByReceiptNumber(candidate).isPresent());
        return candidate;
    }
}
