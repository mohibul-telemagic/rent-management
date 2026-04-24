package com.renteasebd.audit.dto;

import java.time.LocalDateTime;

public record AuditTimelineItemResponse(
    Long id,
    Long actorUserId,
    String action,
    String entityType,
    String entityId,
    LocalDateTime createdAt
) {
}
