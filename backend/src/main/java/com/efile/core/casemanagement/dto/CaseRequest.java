package com.efile.core.casemanagement.dto;

import com.efile.core.casemanagement.CaseStatus;
import com.efile.core.casemanagement.CasePriority;
import com.efile.core.casemanagement.CaseCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CaseRequest(
    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 191, message = "Title must be between 5 and 191 characters")
    String title,

    @NotBlank(message = "Description is required")
    @Size(min = 20, message = "Description must be at least 20 characters")
    String description,

    CaseStatus status,

    Long assignedToId,

    CasePriority priority,

    CaseCategory category,

    List<String> tags,

    String dueDate,

    String estimatedCompletionDate,

    Double budget,

    String location,

    String department

) {
    // Custom constructor to handle null assignedToId properly
    public CaseRequest {
        // No validation needed for nullable fields
    }
}
