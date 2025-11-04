package com.efile.core.document.dto;

import java.time.Instant;
import com.efile.core.document.DocumentStatus;
import com.efile.core.document.DocumentType;

public record DocumentResponse(
    Long id,
    String title,
    DocumentType type,
    DocumentStatus status,
    Long caseId,
    String caseTitle,
    Long uploadedById,
    String uploadedByName,
    Long approvedById,
    String approvedByName,
    long fileSize,
    String filePath,
    String receiptNumber,
    Instant uploadedAt,
    Instant processedAt
) {
}
