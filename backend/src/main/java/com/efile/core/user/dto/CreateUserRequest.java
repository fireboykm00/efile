package com.efile.core.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
    @NotBlank String name,
    @NotBlank @Email String email,
    @NotBlank @Size(min = 12, message = "Password must be at least 12 characters") String password,
    @NotBlank String role,
    Long departmentId,
    Boolean active
) {
}
