package com.connectsphere.post.api.dto;

import java.time.Instant;
import java.util.List;

public record PostResponse(
        String id,
        String authorUserId,
        String caption,
        String mediaUrl,
        Instant createdAt,
        long likeCount,
        long commentCount,
        List<CommentResponse> comments
) {
}

