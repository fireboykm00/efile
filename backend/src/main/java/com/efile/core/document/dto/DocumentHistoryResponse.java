package com.efile.core.document.dto;

import java.time.Instant;
import com.efile.core.document.DocumentStatus;

public record DocumentHistoryResponse(
    DocumentStatus status,
    String comment,
    Instant changedAt
) {
}
