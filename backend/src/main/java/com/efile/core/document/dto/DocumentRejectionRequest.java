package com.efile.core.document.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DocumentRejectionRequest(
    @NotBlank @Size(min = 10) String reason
) {
}
