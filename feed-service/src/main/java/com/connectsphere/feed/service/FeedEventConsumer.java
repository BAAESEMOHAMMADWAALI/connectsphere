package com.connectsphere.feed.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class FeedEventConsumer {

    private final FeedService feedService;

    public FeedEventConsumer(FeedService feedService) {
        this.feedService = feedService;
    }

    // Day 1 uses coarse invalidation to keep the event flow simple while the domain stabilizes.
    @KafkaListener(topics = {"post-created", "user-followed"}, groupId = "feed-service")
    public void invalidateFeedCache(String ignoredPayload) {
        feedService.invalidateAllFeeds();
    }
}

