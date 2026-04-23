package com.connectsphere.notification.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventConsumer.class);

    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;

    public NotificationEventConsumer(ObjectMapper objectMapper, NotificationService notificationService) {
        this.objectMapper = objectMapper;
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = "post-liked", groupId = "${spring.kafka.consumer.group-id:notification-service}")
    public void handlePostLiked(String payload) {
        JsonNode event = readPayload(payload);
        String targetUserId = readText(event, "targetUserId", "toUserId");
        String actorUserId = readText(event, "actorUserId", "fromUserId");
        if (targetUserId == null || actorUserId == null) {
            log.warn("Skipping malformed post-liked event: {}", payload);
            return;
        }
        notificationService.saveNotification(
                UUID.fromString(targetUserId),
                "POST_LIKED",
                "Your post " + readText(event, "postId") + " was liked by " + actorUserId,
                payload
        );
    }

    @KafkaListener(topics = "comment-created", groupId = "${spring.kafka.consumer.group-id:notification-service}")
    public void handleCommentCreated(String payload) {
        JsonNode event = readPayload(payload);
        String targetUserId = readText(event, "targetUserId", "toUserId");
        if (targetUserId == null) {
            log.warn("Skipping malformed comment-created event: {}", payload);
            return;
        }
        notificationService.saveNotification(
                UUID.fromString(targetUserId),
                "COMMENT_CREATED",
                "Your post " + readText(event, "postId") + " received a new comment",
                payload
        );
    }

    @KafkaListener(topics = "user-followed", groupId = "${spring.kafka.consumer.group-id:notification-service}")
    public void handleUserFollowed(String payload) {
        JsonNode event = readPayload(payload);
        String followeeUserId = readText(event, "followeeUserId", "toUserId", "targetUserId");
        String followerUserId = readText(event, "followerUserId", "fromUserId", "actorUserId");
        if (followeeUserId == null || followerUserId == null) {
            log.warn("Skipping malformed user-followed event: {}", payload);
            return;
        }
        notificationService.saveNotification(
                UUID.fromString(followeeUserId),
                "USER_FOLLOWED",
                followerUserId + " started following you",
                payload
        );
    }

    private JsonNode readPayload(String payload) {
        try {
            return objectMapper.readTree(payload);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to parse Kafka payload", exception);
        }
    }

    private String readText(JsonNode event, String... fieldNames) {
        for (String fieldName : fieldNames) {
            JsonNode value = event.get(fieldName);
            if (value != null && !value.isNull()) {
                return value.asText();
            }
        }
        return null;
    }
}
