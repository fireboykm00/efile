package com.efile.core.document;

import com.efile.core.casemanagement.Case;
import com.efile.core.casemanagement.CaseRepository;
import com.efile.core.department.Department;
import com.efile.core.department.DepartmentRepository;
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
    private final DepartmentRepository departmentRepository;
    private final FileStorageService fileStorageService;

    public DocumentService(
        DocumentRepository documentRepository,
        DocumentStatusHistoryRepository historyRepository,
        CaseRepository caseRepository,
        UserRepository userRepository,
        DepartmentRepository departmentRepository,
        FileStorageService fileStorageService
    ) {
        this.documentRepository = documentRepository;
        this.historyRepository = historyRepository;
        this.caseRepository = caseRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
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
        document.setStatus(DocumentStatus.DRAFT);
        document.setCaseRef(caseEntity);
        document.setUploadedBy(uploader);
        document.setReceiptNumber(generateReceiptNumber());
        Document saved = documentRepository.save(document);
        recordHistory(saved, DocumentStatus.DRAFT, "Document uploaded as draft");
        return toResponse(saved);
    }

    @Transactional
    public DocumentResponse approveDocument(Long documentId) {
        Document document = documentRepository.findById(documentId).orElseThrow(() -> new IllegalArgumentException("Document not found"));
        if (document.getStatus() == DocumentStatus.APPROVED) {
            return toResponse(document);
        }
        validateStatusTransition(document.getStatus(), DocumentStatus.APPROVED);
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
        validateStatusTransition(document.getStatus(), DocumentStatus.REJECTED);
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

    @Transactional
    public DocumentResponse submitDocument(Long documentId) {
        Document document = documentRepository.findById(documentId).orElseThrow(() -> new IllegalArgumentException("Document not found"));
        validateStatusTransition(document.getStatus(), DocumentStatus.SUBMITTED);
        ensureCanSubmit(document);
        
        // Route document based on department and document type
        routeDocument(document);
        
        document.setStatus(DocumentStatus.SUBMITTED);
        Document saved = documentRepository.save(document);
        recordHistory(saved, DocumentStatus.SUBMITTED, "Document submitted for review and routed to " + getTargetDepartment(document).getName());
        return toResponse(saved);
    }

    @Transactional
    public DocumentResponse startReview(Long documentId) {
        Document document = documentRepository.findById(documentId).orElseThrow(() -> new IllegalArgumentException("Document not found"));
        validateStatusTransition(document.getStatus(), DocumentStatus.UNDER_REVIEW);
        ensureReviewerRole(currentUser().getRole());
        document.setStatus(DocumentStatus.UNDER_REVIEW);
        Document saved = documentRepository.save(document);
        recordHistory(saved, DocumentStatus.UNDER_REVIEW, "Review started by " + currentUser().getName());
        return toResponse(saved);
    }

    @Transactional
    public DocumentResponse withdrawDocument(Long documentId) {
        Document document = documentRepository.findById(documentId).orElseThrow(() -> new IllegalArgumentException("Document not found"));
        validateStatusTransition(document.getStatus(), DocumentStatus.WITHDRAWN);
        ensureCanWithdraw(document);
        document.setStatus(DocumentStatus.WITHDRAWN);
        document.setProcessedAt(Instant.now());
        Document saved = documentRepository.save(document);
        recordHistory(saved, DocumentStatus.WITHDRAWN, "Document withdrawn by " + currentUser().getName());
        return toResponse(saved);
    }

    private void validateStatusTransition(DocumentStatus currentStatus, DocumentStatus newStatus) {
        switch (currentStatus) {
            case DRAFT:
                if (newStatus != DocumentStatus.SUBMITTED && newStatus != DocumentStatus.WITHDRAWN) {
                    throw new IllegalStateException("Draft documents can only be submitted or withdrawn");
                }
                break;
            case SUBMITTED:
                if (newStatus != DocumentStatus.UNDER_REVIEW && newStatus != DocumentStatus.WITHDRAWN) {
                    throw new IllegalStateException("Submitted documents can only be moved to review or withdrawn");
                }
                break;
            case UNDER_REVIEW:
                if (newStatus != DocumentStatus.APPROVED && newStatus != DocumentStatus.REJECTED) {
                    throw new IllegalStateException("Documents under review can only be approved or rejected");
                }
                break;
            case APPROVED:
                throw new IllegalStateException("Approved documents cannot be changed");
            case REJECTED:
                if (newStatus != DocumentStatus.DRAFT && newStatus != DocumentStatus.WITHDRAWN) {
                    throw new IllegalStateException("Rejected documents can only be edited (draft) or withdrawn");
                }
                break;
            case WITHDRAWN:
                throw new IllegalStateException("Withdrawn documents cannot be changed");
        }
    }

    private void ensureCanSubmit(Document document) {
        User currentUser = currentUser();
        if (!document.getUploadedBy().getId().equals(currentUser.getId()) && 
            currentUser.getRole() != UserRole.ADMIN) {
            throw new IllegalStateException("Only document uploader or admin can submit document");
        }
    }

    private void ensureCanWithdraw(Document document) {
        User currentUser = currentUser();
        if (!document.getUploadedBy().getId().equals(currentUser.getId()) && 
            currentUser.getRole() != UserRole.ADMIN) {
            throw new IllegalStateException("Only document uploader or admin can withdraw document");
        }
    }

    private void ensureReviewerRole(UserRole role) {
        if (!(role == UserRole.CEO || role == UserRole.CFO || role == UserRole.ADMIN || role == UserRole.AUDITOR)) {
            throw new IllegalArgumentException("User not authorized to review documents");
        }
    }

    private void routeDocument(Document document) {
        Department targetDepartment = getTargetDepartment(document);
        // Assign document to department head for review
        if (targetDepartment.getHead() != null) {
            // In a real implementation, you might create assignments or notifications
            // For now, we'll just record the routing in history
            recordHistory(document, document.getStatus(), 
                "Document routed to " + targetDepartment.getName() + " department");
        }
    }

    private Department getTargetDepartment(Document document) {
        // Route based on document type
        switch (document.getType()) {
            case FINANCIAL_REPORT:
            case PROCUREMENT_BID:
                return getDepartmentByName("Finance");
            case LEGAL_DOCUMENT:
                return getDepartmentByName("Legal");
            case AUDIT_REPORT:
                return getDepartmentByName("Audit");
            case INVESTMENT_REPORT:
                return getDepartmentByName("Finance");
            default:
                // Route to general department or user's department
                return document.getUploadedBy().getDepartment();
        }
    }

    private Department getDepartmentByName(String name) {
        return departmentRepository.findByNameIgnoreCase(name)
            .orElseThrow(() -> new IllegalArgumentException("Department not found: " + name));
    }

    @Transactional(readOnly = true)
    public Page<DocumentResponse> searchDocuments(DocumentSearchCriteria criteria, Pageable pageable) {
        Specification<Document> specification = Specification.allOf();
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

    public DocumentResponse toResponse(Document document) {
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
        String timestamp = String.valueOf(Instant.now().toEpochMilli());
        String random = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        String candidate = "EF" + timestamp + "-" + random;
        
        // Ensure uniqueness
        int counter = 1;
        String uniqueCandidate = candidate;
        while (documentRepository.findByReceiptNumber(uniqueCandidate).isPresent()) {
            uniqueCandidate = candidate + "-" + counter;
            counter++;
        }
        return uniqueCandidate;
    }

    @Transactional(readOnly = true)
    public String generateReceiptDocument(Long documentId) {
        Document document = documentRepository.findById(documentId)
            .orElseThrow(() -> new IllegalArgumentException("Document not found"));
        
        StringBuilder receipt = new StringBuilder();
        receipt.append("============================================================\n");
        receipt.append("                    E-FILECONNECT RECEIPT\n");
        receipt.append("============================================================\n\n");
        
        receipt.append("Receipt Number: ").append(document.getReceiptNumber()).append("\n");
        receipt.append("Document ID: ").append(document.getId()).append("\n");
        receipt.append("Document Title: ").append(document.getTitle()).append("\n");
        receipt.append("Document Type: ").append(document.getType()).append("\n");
        receipt.append("File Size: ").append(formatFileSize(document.getFileSize())).append("\n");
        receipt.append("Status: ").append(document.getStatus()).append("\n\n");
        
        receipt.append("Submission Details:\n");
        receipt.append("------------------------------\n");
        receipt.append("Uploaded By: ").append(document.getUploadedBy().getName()).append("\n");
        receipt.append("Email: ").append(document.getUploadedBy().getEmail()).append("\n");
        receipt.append("Upload Date: ").append(document.getUploadedAt()).append("\n");
        
        if (document.getCaseRef() != null) {
            receipt.append("\nCase Information:\n");
            receipt.append("------------------------------\n");
            receipt.append("Case ID: ").append(document.getCaseRef().getId()).append("\n");
            receipt.append("Case Title: ").append(document.getCaseRef().getTitle()).append("\n");
        }
        
        if (document.getApprovedBy() != null) {
            receipt.append("\nApproval Details:\n");
            receipt.append("------------------------------\n");
            receipt.append("Approved By: ").append(document.getApprovedBy().getName()).append("\n");
            receipt.append("Process Date: ").append(document.getProcessedAt()).append("\n");
        }
        
        if (document.getRejectionReason() != null) {
            receipt.append("\nRejection Reason: ").append(document.getRejectionReason()).append("\n");
        }
        
        receipt.append("\n============================================================\n");
        receipt.append("            This is an electronically generated receipt\n");
        receipt.append("                   Valid for record keeping only\n");
        receipt.append("============================================================\n");
        
        return receipt.toString();
    }

    private String formatFileSize(Long size) {
        if (size == null) return "Unknown";
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
        return String.format("%.1f MB", size / (1024.0 * 1024.0));
    }
}
