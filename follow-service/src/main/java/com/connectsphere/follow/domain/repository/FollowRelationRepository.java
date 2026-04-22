package com.connectsphere.follow.domain.repository;

import com.connectsphere.follow.domain.entity.FollowRelation;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FollowRelationRepository extends JpaRepository<FollowRelation, UUID> {

    boolean existsByFollowerUserIdAndFolloweeUserId(UUID followerUserId, UUID followeeUserId);

    void deleteByFollowerUserIdAndFolloweeUserId(UUID followerUserId, UUID followeeUserId);

    List<FollowRelation> findByFollowerUserIdOrderByCreatedAtDesc(UUID followerUserId);

    List<FollowRelation> findByFolloweeUserIdOrderByCreatedAtDesc(UUID followeeUserId);
}

