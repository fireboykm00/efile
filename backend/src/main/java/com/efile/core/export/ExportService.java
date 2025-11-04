package com.efile.core.export;

import com.efile.core.casemanagement.Case;
import com.efile.core.casemanagement.CaseRepository;
import com.efile.core.document.Document;
import com.efile.core.document.DocumentRepository;
import com.efile.core.document.DocumentType;
import com.efile.core.user.User;
import com.efile.core.user.UserRepository;
import com.efile.core.user.UserRole;
import jakarta.persistence.EntityNotFoundException;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ExportService {

    private final DocumentRepository documentRepository;
    private final CaseRepository caseRepository;
    private final UserRepository userRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_INSTANT;

    public ExportService(
        DocumentRepository documentRepository,
        CaseRepository caseRepository,
        UserRepository userRepository
    ) {
        this.documentRepository = documentRepository;
        this.caseRepository = caseRepository;
        this.userRepository = userRepository;
    }

    public byte[] exportDocumentsAsCsv() {
        User currentUser = getCurrentUser();
        List<Document> documents = getDocumentsForUser(currentUser);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(outputStream, true, StandardCharsets.UTF_8);

        // Write CSV header
        writer.println("ID,Title,Type,Status,Case ID,Uploaded By,Uploaded At,File Size");

        // Write data rows
        for (Document doc : documents) {
            writer.printf("%d,\"%s\",%s,%s,%d,\"%s\",%s,%d%n",
                doc.getId(),
                escapeCsv(doc.getTitle()),
                doc.getType(),
                doc.getStatus(),
                doc.getCaseRef() != null ? doc.getCaseRef().getId() : 0,
                doc.getUploadedBy() != null ? escapeCsv(doc.getUploadedBy().getName()) : "",
                doc.getUploadedAt() != null ? DATE_FORMATTER.format(doc.getUploadedAt()) : "",
                doc.getFileSize()
            );
        }

        writer.flush();
        return outputStream.toByteArray();
    }

    public byte[] exportCasesAsCsv() {
        User currentUser = getCurrentUser();
        List<Case> cases = getCasesForUser(currentUser);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(outputStream, true, StandardCharsets.UTF_8);

        // Write CSV header
        writer.println("ID,Title,Description,Status,Assigned To,Created By,Created At");

        // Write data rows
        for (Case caseEntity : cases) {
            writer.printf("%d,\"%s\",\"%s\",%s,\"%s\",\"%s\",%s%n",
                caseEntity.getId(),
                escapeCsv(caseEntity.getTitle()),
                escapeCsv(caseEntity.getDescription()),
                caseEntity.getStatus(),
                caseEntity.getAssignedTo() != null ? escapeCsv(caseEntity.getAssignedTo().getName()) : "",
                caseEntity.getCreatedBy() != null ? escapeCsv(caseEntity.getCreatedBy().getName()) : "",
                caseEntity.getCreatedAt() != null ? DATE_FORMATTER.format(caseEntity.getCreatedAt()) : ""
            );
        }

        writer.flush();
        return outputStream.toByteArray();
    }

    private List<Document> getDocumentsForUser(User user) {
        // INVESTOR can only export investment reports
        if (user.getRole() == UserRole.INVESTOR) {
            return documentRepository.findByType(DocumentType.INVESTMENT_REPORT, null).getContent();
        }
        
        // Others can export all documents they have access to
        return documentRepository.findAll();
    }

    private List<Case> getCasesForUser(User user) {
        // Role-based filtering
        if (user.getRole() == UserRole.ADMIN || 
            user.getRole() == UserRole.CEO || 
            user.getRole() == UserRole.CFO || 
            user.getRole() == UserRole.AUDITOR) {
            return caseRepository.findAll();
        } else {
            return caseRepository.findByCreatedByOrAssignedTo(user, user);
        }
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        // Escape quotes by doubling them
        return value.replace("\"", "\"\"");
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmailIgnoreCase(email)
            .orElseThrow(() -> new EntityNotFoundException("Current user not found"));
    }
}
