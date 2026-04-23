package com.connectsphere.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.connectsphere.notification.api.dto.NotificationResponse;
import com.connectsphere.notification.domain.entity.NotificationEntity;
import com.connectsphere.notification.domain.repository.NotificationRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void returnsNotificationsNewestFirstFromRepository() {
        UUID recipientUserId = UUID.randomUUID();
        NotificationEntity notification = notification(recipientUserId, "USER_FOLLOWED", false);
        when(notificationRepository.findByRecipientUserIdOrderByCreatedAtDesc(recipientUserId))
                .thenReturn(List.of(notification));

        List<NotificationResponse> responses = notificationService.getNotifications(recipientUserId.toString());

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).recipientUserId()).isEqualTo(recipientUserId.toString());
        assertThat(responses.get(0).read()).isFalse();
    }

    @Test
    void markAsReadWithoutUserUpdatesNotification() {
        UUID recipientUserId = UUID.randomUUID();
        UUID notificationId = UUID.randomUUID();
        NotificationEntity notification = notification(recipientUserId, "POST_LIKED", false);
        ReflectionTestUtils.setField(notification, "id", notificationId);
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));

        NotificationResponse response = notificationService.markAsRead(notificationId.toString());

        assertThat(response.read()).isTrue();
        assertThat(notification.isRead()).isTrue();
    }

    @Test
    void saveNotificationPersistsUnreadNotification() {
        UUID recipientUserId = UUID.randomUUID();
        String payload = "{\"type\":\"FOLLOW\"}";

        notificationService.saveNotification(recipientUserId, "USER_FOLLOWED", "Someone followed you", payload);

        verify(notificationRepository).save(org.mockito.ArgumentMatchers.argThat(notification ->
                notification.getRecipientUserId().equals(recipientUserId)
                        && notification.getType().equals("USER_FOLLOWED")
                        && !notification.isRead()
                        && notification.getPayloadJson().equals(payload)
        ));
    }

    private NotificationEntity notification(UUID recipientUserId, String type, boolean read) {
        NotificationEntity notification = new NotificationEntity();
        ReflectionTestUtils.setField(notification, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(notification, "createdAt", Instant.parse("2026-04-23T09:00:00Z"));
        notification.setRecipientUserId(recipientUserId);
        notification.setType(type);
        notification.setMessage("Message");
        notification.setRead(read);
        notification.setPayloadJson("{}");
        return notification;
    }
}
