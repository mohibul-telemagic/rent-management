package com.renteasebd.auth.dto;

import java.time.LocalDateTime;

public record AuthSessionResponse(
    String id,
    String deviceHint,
    LocalDateTime createdAt,
    LocalDateTime expiresAt
) {
}
