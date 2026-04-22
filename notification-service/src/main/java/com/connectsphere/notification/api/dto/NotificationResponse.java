package com.connectsphere.notification.api.dto;

import java.time.Instant;

public record NotificationResponse(
        String id,
        String recipientUserId,
        String type,
        String message,
        boolean read,
        String payloadJson,
        Instant createdAt
) {
}

