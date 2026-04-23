package com.connectsphere.feed.api.dto;

import java.util.List;

public record FeedResponse(
        String userId,
        int page,
        int size,
        List<FeedItemResponse> items,
        boolean cacheHit
) {
}
