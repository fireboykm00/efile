package com.efile.core.reporting;

import com.efile.core.casemanagement.CaseRepository;
import com.efile.core.communication.CommunicationRepository;
import com.efile.core.document.DocumentRepository;
import com.efile.core.user.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReportService {

    private final DocumentRepository documentRepository;
    private final CaseRepository caseRepository;
    private final UserRepository userRepository;
    private final CommunicationRepository communicationRepository;

    public ReportService(
        DocumentRepository documentRepository,
        CaseRepository caseRepository,
        UserRepository userRepository,
        CommunicationRepository communicationRepository
    ) {
        this.documentRepository = documentRepository;
        this.caseRepository = caseRepository;
        this.userRepository = userRepository;
        this.communicationRepository = communicationRepository;
    }

    public String generateDocumentsReport(String format) {
        if ("csv".equalsIgnoreCase(format)) {
            return generateDocumentsCSV();
        }
        throw new IllegalArgumentException("Unsupported format: " + format);
    }

    public String generateCasesReport(String format) {
        if ("csv".equalsIgnoreCase(format)) {
            return generateCasesCSV();
        }
        throw new IllegalArgumentException("Unsupported format: " + format);
    }

    public String generateUsersReport(String format) {
        if ("csv".equalsIgnoreCase(format)) {
            return generateUsersCSV();
        }
        throw new IllegalArgumentException("Unsupported format: " + format);
    }

    public String generateCommunicationsReport(String format) {
        if ("csv".equalsIgnoreCase(format)) {
            return generateCommunicationsCSV();
        }
        throw new IllegalArgumentException("Unsupported format: " + format);
    }

    private String generateDocumentsCSV() {
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Title,Type,Status,Uploaded By,Uploaded At,Processed At,File Size,Receipt Number\n");
        
        documentRepository.findAll().forEach(doc -> {
            csv.append(doc.getId()).append(",");
            csv.append(escapeCSV(doc.getTitle())).append(",");
            csv.append(doc.getType()).append(",");
            csv.append(doc.getStatus()).append(",");
            csv.append(doc.getUploadedBy() != null ? escapeCSV(doc.getUploadedBy().getName()) : "").append(",");
            csv.append(doc.getUploadedAt()).append(",");
            csv.append(doc.getProcessedAt() != null ? doc.getProcessedAt() : "").append(",");
            csv.append(doc.getFileSize()).append(",");
            csv.append(doc.getReceiptNumber() != null ? doc.getReceiptNumber() : "").append("\n");
        });
        
        return csv.toString();
    }

    private String generateCasesCSV() {
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Title,Description,Status,Created By,Assigned To,Created At,Updated At\n");
        
        caseRepository.findAll().forEach(caseEntity -> {
            csv.append(caseEntity.getId()).append(",");
            csv.append(escapeCSV(caseEntity.getTitle())).append(",");
            csv.append(escapeCSV(caseEntity.getDescription())).append(",");
            csv.append(caseEntity.getStatus()).append(",");
            csv.append(caseEntity.getCreatedBy() != null ? escapeCSV(caseEntity.getCreatedBy().getName()) : "").append(",");
            csv.append(caseEntity.getAssignedTo() != null ? escapeCSV(caseEntity.getAssignedTo().getName()) : "").append(",");
            csv.append(caseEntity.getCreatedAt()).append(",");
            csv.append(caseEntity.getUpdatedAt()).append("\n");
        });
        
        return csv.toString();
    }

    private String generateUsersCSV() {
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Name,Email,Role,Department,Is Active,Created At\n");
        
        userRepository.findAll().forEach(user -> {
            csv.append(user.getId()).append(",");
            csv.append(escapeCSV(user.getName())).append(",");
            csv.append(user.getEmail()).append(",");
            csv.append(user.getRole()).append(",");
            csv.append(user.getDepartment() != null ? escapeCSV(user.getDepartment().getName()) : "").append(",");
            csv.append(user.isActive()).append(",");
            csv.append(user.getCreatedAt()).append("\n");
        });
        
        return csv.toString();
    }

    private String generateCommunicationsCSV() {
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Type,Content,Sender,Recipient,Case Title,Sent At,Read At\n");
        
        communicationRepository.findAll().forEach(comm -> {
            csv.append(comm.getId()).append(",");
            csv.append(comm.getType()).append(",");
            csv.append(escapeCSV(comm.getContent())).append(",");
            csv.append(comm.getSender() != null ? escapeCSV(comm.getSender().getName()) : "").append(",");
            csv.append(comm.getRecipient() != null ? escapeCSV(comm.getRecipient().getName()) : "").append(",");
            csv.append(comm.getCaseRef() != null ? escapeCSV(comm.getCaseRef().getTitle()) : "").append(",");
            csv.append(comm.getSentAt()).append(",");
            csv.append(comm.getReadAt() != null ? comm.getReadAt() : "").append("\n");
        });
        
        return csv.toString();
    }

    private String escapeCSV(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
