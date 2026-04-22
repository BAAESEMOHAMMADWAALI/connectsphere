package com.connectsphere.post.api.dto;

import java.time.Instant;

public record CommentResponse(
        String id,
        String userId,
        String text,
        Instant createdAt
) {
}

