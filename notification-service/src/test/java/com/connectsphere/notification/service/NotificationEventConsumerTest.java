package com.connectsphere.notification.service;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class NotificationEventConsumerTest {

    private final NotificationService notificationService = mock(NotificationService.class);
    private final NotificationEventConsumer consumer = new NotificationEventConsumer(new ObjectMapper(), notificationService);

    @Test
    void createsFollowNotificationFromPromptStyleEvent() {
        UUID followerUserId = UUID.randomUUID();
        UUID followeeUserId = UUID.randomUUID();
        String payload = """
                {
                  "type": "FOLLOW",
                  "fromUserId": "%s",
                  "toUserId": "%s"
                }
                """.formatted(followerUserId, followeeUserId);

        consumer.handleUserFollowed(payload);

        verify(notificationService).saveNotification(
                eq(followeeUserId),
                eq("USER_FOLLOWED"),
                contains(followerUserId.toString()),
                eq(payload)
        );
    }

    @Test
    void createsLikeNotificationFromPostEvent() {
        UUID actorUserId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();
        String payload = """
                {
                  "postId": "%s",
                  "actorUserId": "%s",
                  "targetUserId": "%s"
                }
                """.formatted(UUID.randomUUID(), actorUserId, targetUserId);

        consumer.handlePostLiked(payload);

        verify(notificationService).saveNotification(
                eq(targetUserId),
                eq("POST_LIKED"),
                contains(actorUserId.toString()),
                eq(payload)
        );
    }
}
