package com.connectsphere.user.api.dto;

import java.time.Instant;

public record UserProfileResponse(
        String id,
        String email,
        String fullName,
        String displayName,
        String profileImageUrl,
        String bio,
        String role,
        Instant createdAt
) {
}
