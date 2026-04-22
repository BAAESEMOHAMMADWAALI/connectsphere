package com.connectsphere.user.events;

import java.time.Instant;

public record UserRegisteredEvent(
        String userId,
        String email,
        String displayName,
        Instant registeredAt
) {
}

