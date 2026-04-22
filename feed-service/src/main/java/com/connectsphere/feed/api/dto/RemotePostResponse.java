package com.connectsphere.feed.api.dto;

import java.time.Instant;
import java.util.List;

public record RemotePostResponse(
        String id,
        String authorUserId,
        String caption,
        String mediaUrl,
        Instant createdAt,
        long likeCount,
        long commentCount,
        List<Object> comments
) {
}

