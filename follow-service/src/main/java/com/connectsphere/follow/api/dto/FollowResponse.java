package com.connectsphere.follow.api.dto;

import java.time.Instant;

public record FollowResponse(
        String followerUserId,
        String followeeUserId,
        Instant createdAt
) {
}

