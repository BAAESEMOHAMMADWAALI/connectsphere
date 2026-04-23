package com.connectsphere.notification.service;

import com.connectsphere.notification.api.dto.NotificationResponse;
import com.connectsphere.notification.domain.entity.NotificationEntity;
import com.connectsphere.notification.domain.repository.NotificationRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotifications(String userId) {
        UUID recipientUserId = parseUuid(userId, "user id");
        return notificationRepository.findByRecipientUserIdOrderByCreatedAtDesc(recipientUserId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public NotificationResponse markAsRead(String userId, String notificationId) {
        NotificationEntity notificationEntity = notificationRepository.findById(parseUuid(notificationId, "notification id"))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));

        if (!notificationEntity.getRecipientUserId().equals(parseUuid(userId, "user id"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Notification does not belong to the user");
        }

        notificationEntity.setRead(true);
        return mapToResponse(notificationEntity);
    }

    @Transactional
    public NotificationResponse markAsRead(String notificationId) {
        NotificationEntity notificationEntity = notificationRepository.findById(parseUuid(notificationId, "notification id"))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));

        notificationEntity.setRead(true);
        return mapToResponse(notificationEntity);
    }

    @Transactional
    public void saveNotification(UUID recipientUserId, String type, String message, String payloadJson) {
        NotificationEntity notificationEntity = new NotificationEntity();
        notificationEntity.setRecipientUserId(recipientUserId);
        notificationEntity.setType(type);
        notificationEntity.setMessage(message);
        notificationEntity.setPayloadJson(payloadJson);
        notificationEntity.setRead(false);
        notificationRepository.save(notificationEntity);
    }

    private NotificationResponse mapToResponse(NotificationEntity notificationEntity) {
        return new NotificationResponse(
                notificationEntity.getId().toString(),
                notificationEntity.getRecipientUserId().toString(),
                notificationEntity.getType(),
                notificationEntity.getMessage(),
                notificationEntity.isRead(),
                notificationEntity.getPayloadJson(),
                notificationEntity.getCreatedAt()
        );
    }

    private UUID parseUuid(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing " + fieldName);
        }
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid " + fieldName, exception);
        }
    }
}
