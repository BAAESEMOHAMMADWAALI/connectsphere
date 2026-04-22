package com.connectsphere.follow.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "follow_relations",
        uniqueConstraints = @UniqueConstraint(name = "uk_follower_followee", columnNames = {"follower_user_id", "followee_user_id"})
)
public class FollowRelation {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID followerUserId;

    @Column(nullable = false)
    private UUID followeeUserId;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getFollowerUserId() {
        return followerUserId;
    }

    public void setFollowerUserId(UUID followerUserId) {
        this.followerUserId = followerUserId;
    }

    public UUID getFolloweeUserId() {
        return followeeUserId;
    }

    public void setFolloweeUserId(UUID followeeUserId) {
        this.followeeUserId = followeeUserId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

