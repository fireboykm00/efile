package com.efile.core.casemanagement.dto;

import com.efile.core.casemanagement.CaseStatus;
import com.efile.core.casemanagement.CasePriority;
import com.efile.core.casemanagement.CaseCategory;
import com.efile.core.document.dto.DocumentResponse;
import com.efile.core.user.dto.UserSummary;
import java.time.Instant;
import java.util.List;

public record CaseResponse(
    Long id,
    String title,
    String description,
    CaseStatus status,
    CasePriority priority,
    CaseCategory category,
    List<String> tags,
    Instant dueDate,
    Instant estimatedCompletionDate,
    Double budget,
    String location,
    String department,
    UserSummary assignedTo,
    UserSummary createdBy,
    Instant createdAt,
    Instant updatedAt,
    List<DocumentResponse> documents
) {}
