package com.efile.core.auth.dto;

import com.efile.core.user.User;

public record LoginResponse(
    boolean success,
    String message,
    UserData user,
    String token
) {
    public static record UserData(
        Long id,
        String name,
        String email,
        String role,
        boolean isActive
    ) {
        public static UserData from(User user) {
            return new UserData(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                user.isActive()
            );
        }
    }
}