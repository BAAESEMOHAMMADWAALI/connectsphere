package com.connectsphere.follow.service;

import com.connectsphere.follow.api.dto.FollowResponse;
import com.connectsphere.follow.domain.entity.FollowRelation;
import com.connectsphere.follow.domain.repository.FollowRelationRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class FollowService {

    private final FollowRelationRepository followRelationRepository;
    private final FollowEventPublisher followEventPublisher;

    public FollowService(FollowRelationRepository followRelationRepository, FollowEventPublisher followEventPublisher) {
        this.followRelationRepository = followRelationRepository;
        this.followEventPublisher = followEventPublisher;
    }

    @Transactional
    public FollowResponse followUser(String followerUserId, String followeeUserId) {
        UUID followerId = parseUuid(followerUserId, "follower user id");
        UUID followeeId = parseUuid(followeeUserId, "followee user id");

        if (followerId.equals(followeeId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Users cannot follow themselves");
        }

        if (followRelationRepository.existsByFollowerUserIdAndFolloweeUserId(followerId, followeeId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Follow relation already exists");
        }

        FollowRelation followRelation = new FollowRelation();
        followRelation.setFollowerUserId(followerId);
        followRelation.setFolloweeUserId(followeeId);

        FollowRelation savedRelation = followRelationRepository.save(followRelation);
        followEventPublisher.publishFollowCreated(savedRelation);
        return mapToResponse(savedRelation);
    }

    @Transactional
    public void unfollowUser(String followerUserId, String followeeUserId) {
        followRelationRepository.deleteByFollowerUserIdAndFolloweeUserId(
                parseUuid(followerUserId, "follower user id"),
                parseUuid(followeeUserId, "followee user id")
        );
    }

    @Transactional(readOnly = true)
    public List<String> getFollowers(String userId) {
        UUID followeeId = parseUuid(userId, "user id");
        return followRelationRepository.findByFolloweeUserIdOrderByCreatedAtDesc(followeeId).stream()
                .map(relation -> relation.getFollowerUserId().toString())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<String> getFollowing(String userId) {
        UUID followerId = parseUuid(userId, "user id");
        return followRelationRepository.findByFollowerUserIdOrderByCreatedAtDesc(followerId).stream()
                .map(relation -> relation.getFolloweeUserId().toString())
                .toList();
    }

    private FollowResponse mapToResponse(FollowRelation followRelation) {
        return new FollowResponse(
                followRelation.getFollowerUserId().toString(),
                followRelation.getFolloweeUserId().toString(),
                followRelation.getCreatedAt()
        );
    }

    private UUID parseUuid(String value, String fieldName) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid " + fieldName, exception);
        }
    }
}

