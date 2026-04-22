package com.connectsphere.notification.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.UUID;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventConsumer {

    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;

    public NotificationEventConsumer(ObjectMapper objectMapper, NotificationService notificationService) {
        this.objectMapper = objectMapper;
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = "post-liked", groupId = "notification-service")
    public void handlePostLiked(String payload) {
        JsonNode event = readPayload(payload);
        notificationService.saveNotification(
                UUID.fromString(event.get("targetUserId").asText()),
                "POST_LIKED",
                "Your post " + event.get("postId").asText() + " was liked by " + event.get("actorUserId").asText(),
                payload
        );
    }

    @KafkaListener(topics = "comment-created", groupId = "notification-service")
    public void handleCommentCreated(String payload) {
        JsonNode event = readPayload(payload);
        notificationService.saveNotification(
                UUID.fromString(event.get("targetUserId").asText()),
                "COMMENT_CREATED",
                "Your post " + event.get("postId").asText() + " received a new comment",
                payload
        );
    }

    @KafkaListener(topics = "user-followed", groupId = "notification-service")
    public void handleUserFollowed(String payload) {
        JsonNode event = readPayload(payload);
        notificationService.saveNotification(
                UUID.fromString(event.get("followeeUserId").asText()),
                "USER_FOLLOWED",
                event.get("followerUserId").asText() + " started following you",
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
}

