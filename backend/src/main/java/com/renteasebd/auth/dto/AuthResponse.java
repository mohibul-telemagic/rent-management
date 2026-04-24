package com.renteasebd.auth.dto;

public record AuthResponse(
    String accessToken,
    String refreshToken,
    long accessTokenExpiresInSeconds,
    String preferredLanguage,
    String role
) {
}
