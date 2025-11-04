package com.efile.core.casemanagement.dto;

import com.efile.core.casemanagement.CaseStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CaseRequest(
    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 191, message = "Title must be between 5 and 191 characters")
    String title,

    @NotBlank(message = "Description is required")
    @Size(min = 20, message = "Description must be at least 20 characters")
    String description,

    CaseStatus status,

    Long assignedToId
) {}
