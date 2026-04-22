package com.connectsphere.post.events;

import java.time.Instant;

public record PostLikedEvent(
        String postId,
        String actorUserId,
        String targetUserId,
        Instant createdAt
) {
}

