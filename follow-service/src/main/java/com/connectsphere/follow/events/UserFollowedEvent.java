package com.connectsphere.follow.events;

import java.time.Instant;

public record UserFollowedEvent(
        String followerUserId,
        String followeeUserId,
        Instant createdAt
) {
}

