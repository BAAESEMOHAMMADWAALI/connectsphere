package com.connectsphere.user.api.dto;

public record TokenResponse(
        String token,
        String userId,
        String role,
        String tokenType
) {

    public static TokenResponse bearer(String userId, String token, String role) {
        return new TokenResponse(token, userId, role, "Bearer");
    }
}
