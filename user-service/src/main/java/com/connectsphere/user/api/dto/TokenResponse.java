package com.connectsphere.user.api.dto;

public record TokenResponse(
        String userId,
        String accessToken,
        String tokenType
) {
}

