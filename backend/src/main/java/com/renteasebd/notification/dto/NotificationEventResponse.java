package com.renteasebd.notification.dto;

import java.time.LocalDateTime;

public record NotificationEventResponse(
    Long id,
    Long tenantId,
    String eventType,
    String channel,
    String status,
    String destination,
    String message,
    String relatedEntityType,
    String relatedEntityId,
    String errorMessage,
    LocalDateTime createdAt,
    LocalDateTime sentAt
) {
}
