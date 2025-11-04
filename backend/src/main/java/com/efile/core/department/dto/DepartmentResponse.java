package com.efile.core.department.dto;

import java.time.Instant;
import com.efile.core.user.dto.UserSummary;

public record DepartmentResponse(
    Long id,
    String name,
    UserSummary head,
    Instant createdAt,
    Instant updatedAt
) {
}
