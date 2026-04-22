package com.connectsphere.notification.domain.repository;

import com.connectsphere.notification.domain.entity.NotificationEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<NotificationEntity, UUID> {

    List<NotificationEntity> findByRecipientUserIdOrderByCreatedAtDesc(UUID recipientUserId);
}

