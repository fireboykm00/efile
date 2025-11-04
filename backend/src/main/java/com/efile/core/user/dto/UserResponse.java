package com.efile.core.user.dto;

public record UserResponse(
    Long id,
    String name,
    String email,
    String role,
    boolean active,
    DepartmentSummary department
) {
    public record DepartmentSummary(Long id, String name) {
    }
}
