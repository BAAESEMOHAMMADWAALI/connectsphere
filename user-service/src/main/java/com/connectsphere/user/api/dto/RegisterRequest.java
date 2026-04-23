package com.connectsphere.user.api.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.util.StringUtils;

public record RegisterRequest(
        @Email @NotBlank String email,
        @Size(min = 3, max = 100) String fullName,
        @Size(min = 3, max = 100) String displayName,
        @NotBlank @Size(min = 6, max = 100) String password,
        @Size(max = 500) String profileImageUrl,
        @Size(max = 280) String bio
) {

    @AssertTrue(message = "fullName is required")
    public boolean isNameProvided() {
        return StringUtils.hasText(fullName) || StringUtils.hasText(displayName);
    }

    public String resolvedFullName() {
        return StringUtils.hasText(fullName) ? fullName.trim() : displayName.trim();
    }

    public String normalizedProfileImageUrl() {
        return StringUtils.hasText(profileImageUrl) ? profileImageUrl.trim() : "";
    }

    public String normalizedBio() {
        return StringUtils.hasText(bio) ? bio.trim() : "";
    }
}
