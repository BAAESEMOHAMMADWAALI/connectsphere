package com.connectsphere.user.api.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;
import org.springframework.util.StringUtils;

public record UpdateProfileRequest(
        @Size(min = 3, max = 100) String fullName,
        @Size(min = 3, max = 100) String displayName,
        @Size(max = 280) String bio,
        @Size(max = 500) String profileImageUrl
) {

    @AssertTrue(message = "fullName is required")
    public boolean isNameProvided() {
        return StringUtils.hasText(fullName) || StringUtils.hasText(displayName);
    }

    public String resolvedFullName() {
        return StringUtils.hasText(fullName) ? fullName.trim() : displayName.trim();
    }

    public String normalizedBio() {
        return StringUtils.hasText(bio) ? bio.trim() : "";
    }

    public String normalizedProfileImageUrl() {
        return StringUtils.hasText(profileImageUrl) ? profileImageUrl.trim() : "";
    }
}
