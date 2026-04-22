package com.connectsphere.post.events;

import java.time.Instant;

public record PostCreatedEvent(
        String postId,
        String authorUserId,
        String caption,
        String mediaUrl,
        Instant createdAt
) {
}

