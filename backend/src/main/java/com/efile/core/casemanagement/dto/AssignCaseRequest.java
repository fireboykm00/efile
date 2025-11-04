package com.efile.core.casemanagement.dto;

import jakarta.validation.constraints.NotNull;

public record AssignCaseRequest(
    @NotNull(message = "User ID is required")
    Long userId
) {}
