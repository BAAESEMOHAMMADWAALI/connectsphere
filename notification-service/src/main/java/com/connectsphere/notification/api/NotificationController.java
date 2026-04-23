package com.connectsphere.notification.api;

import com.connectsphere.notification.api.dto.NotificationResponse;
import com.connectsphere.notification.service.NotificationService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/api/v1/notifications")
    public List<NotificationResponse> getNotifications(@RequestHeader("X-User-Id") String userId) {
        return notificationService.getNotifications(userId);
    }

    @GetMapping({"/api/notifications/{userId}", "/api/v1/notifications/{userId}"})
    public List<NotificationResponse> getNotificationsForUser(@PathVariable String userId) {
        return notificationService.getNotifications(userId);
    }

    @PatchMapping("/api/v1/notifications/{notificationId}/read")
    public NotificationResponse markAsRead(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String notificationId
    ) {
        return notificationService.markAsRead(userId, notificationId);
    }

    @PutMapping({"/api/notifications/read/{notificationId}", "/api/v1/notifications/read/{notificationId}"})
    public NotificationResponse markAsRead(@PathVariable String notificationId) {
        return notificationService.markAsRead(notificationId);
    }
}
