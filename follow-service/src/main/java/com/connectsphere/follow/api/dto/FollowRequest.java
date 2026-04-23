package com.connectsphere.follow.api.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import org.springframework.util.StringUtils;

public record FollowRequest(
        @NotBlank String followerId,
        String followingId,
        String followeeId
) {

    @AssertTrue(message = "followingId is required")
    public boolean isFollowingProvided() {
        return StringUtils.hasText(followingId) || StringUtils.hasText(followeeId);
    }

    public String resolvedFollowingId() {
        return StringUtils.hasText(followingId) ? followingId.trim() : followeeId.trim();
    }

    public String normalizedFollowerId() {
        return followerId.trim();
    }
}
