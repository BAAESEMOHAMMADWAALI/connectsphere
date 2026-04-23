package com.connectsphere.feed.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class FeedEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(FeedEventConsumer.class);

    private final FeedService feedService;
    private final ObjectMapper objectMapper;

    public FeedEventConsumer(FeedService feedService, ObjectMapper objectMapper) {
        this.feedService = feedService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "post-created", groupId = "${spring.kafka.consumer.group-id:feed-service}")
    public void invalidateFeedCacheForNewPost(String payload) {
        JsonNode event = readPayload(payload);
        JsonNode authorUserId = event.get("authorUserId");
        if (authorUserId == null || authorUserId.isNull()) {
            log.warn("Skipping malformed post-created feed invalidation event: {}", payload);
            return;
        }
        feedService.invalidateFeedsForPostAuthor(authorUserId.asText());
    }

    @KafkaListener(topics = "user-followed", groupId = "${spring.kafka.consumer.group-id:feed-service}")
    public void invalidateFeedCacheForFollow(String payload) {
        JsonNode event = readPayload(payload);
        JsonNode followerUserId = event.hasNonNull("followerUserId") ? event.get("followerUserId") : event.get("fromUserId");
        if (followerUserId != null) {
            feedService.invalidateFeedForUser(followerUserId.asText());
        } else {
            feedService.invalidateAllFeeds();
        }
    }

    private JsonNode readPayload(String payload) {
        try {
            return objectMapper.readTree(payload);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to parse feed event payload", exception);
        }
    }
}
