package com.connectsphere.post.events;

import java.time.Instant;

public record CommentCreatedEvent(
        String postId,
        String commentId,
        String actorUserId,
        String targetUserId,
        String text,
        Instant createdAt
) {
}

