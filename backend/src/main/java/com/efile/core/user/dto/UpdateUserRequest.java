package com.efile.core.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
    String name,
    @Email String email,
    @Size(min = 12, message = "Password must be at least 12 characters") String password,
    String role,
    Long departmentId,
    Boolean active
) {
}
