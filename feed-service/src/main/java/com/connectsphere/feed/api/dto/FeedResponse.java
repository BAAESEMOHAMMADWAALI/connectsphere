package com.connectsphere.feed.api.dto;

import java.util.List;

public record FeedResponse(
        String userId,
        List<FeedItemResponse> items,
        boolean cacheHit
) {
}

