package com.efile.core.document.dto;

import java.time.Instant;
import java.util.Optional;
import com.efile.core.document.DocumentStatus;
import com.efile.core.document.DocumentType;

public record DocumentSearchCriteria(
    Optional<DocumentStatus> status,
    Optional<DocumentType> type,
    Optional<Instant> uploadedAfter,
    Optional<Instant> uploadedBefore,
    Optional<Long> caseId,
    Optional<String> titleKeyword
) {
    public static DocumentSearchCriteria of(
        DocumentStatus status,
        DocumentType type,
        Instant uploadedAfter,
        Instant uploadedBefore,
        Long caseId,
        String titleKeyword
    ) {
        return new DocumentSearchCriteria(
            Optional.ofNullable(status),
            Optional.ofNullable(type),
            Optional.ofNullable(uploadedAfter),
            Optional.ofNullable(uploadedBefore),
            Optional.ofNullable(caseId),
            Optional.ofNullable(titleKeyword).filter(s -> !s.isBlank())
        );
    }
}
