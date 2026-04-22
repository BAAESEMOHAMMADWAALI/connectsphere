package com.connectsphere.feed.api.dto;

import java.time.Instant;

public record FeedItemResponse(
        String id,
        String authorUserId,
        String caption,
        String mediaUrl,
        Instant createdAt,
        long likeCount,
        long commentCount
) {
}

