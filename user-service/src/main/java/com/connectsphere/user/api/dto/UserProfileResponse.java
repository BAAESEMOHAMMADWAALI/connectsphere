package com.connectsphere.user.api.dto;

import java.time.Instant;

public record UserProfileResponse(
        String id,
        String email,
        String displayName,
        String bio,
        Instant createdAt
) {
}

