package com.efile.core.auth.dto;

import java.time.Instant;

public record TokenResponse(String accessToken, String refreshToken, Instant accessTokenExpiresAt) {
}
