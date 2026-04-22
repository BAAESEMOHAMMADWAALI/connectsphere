package com.connectsphere.notification.api;

import com.connectsphere.notification.api.dto.NotificationResponse;
import com.connectsphere.notification.service.NotificationService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public List<NotificationResponse> getNotifications(@RequestHeader("X-User-Id") String userId) {
        return notificationService.getNotifications(userId);
    }

    @PatchMapping("/{notificationId}/read")
    public NotificationResponse markAsRead(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String notificationId
    ) {
        return notificationService.markAsRead(userId, notificationId);
    }
}

