package com.connectsphere.follow.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.connectsphere.follow.api.dto.FollowResponse;
import com.connectsphere.follow.domain.entity.FollowRelation;
import com.connectsphere.follow.domain.repository.FollowRelationRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class FollowServiceTest {

    @Mock
    private FollowRelationRepository followRelationRepository;

    @Mock
    private FollowEventPublisher followEventPublisher;

    @InjectMocks
    private FollowService followService;

    @Test
    void followUserCreatesRelationAndPublishesEvent() {
        UUID followerId = UUID.randomUUID();
        UUID followeeId = UUID.randomUUID();

        when(followRelationRepository.existsByFollowerUserIdAndFolloweeUserId(followerId, followeeId)).thenReturn(false);
        when(followRelationRepository.save(any(FollowRelation.class))).thenAnswer(invocation -> {
            FollowRelation relation = invocation.getArgument(0);
            ReflectionTestUtils.setField(relation, "id", UUID.randomUUID());
            ReflectionTestUtils.setField(relation, "createdAt", Instant.parse("2026-04-23T09:00:00Z"));
            return relation;
        });

        FollowResponse response = followService.followUser(followerId.toString(), followeeId.toString());

        assertThat(response.followerUserId()).isEqualTo(followerId.toString());
        assertThat(response.followeeUserId()).isEqualTo(followeeId.toString());

        ArgumentCaptor<FollowRelation> relationCaptor = ArgumentCaptor.forClass(FollowRelation.class);
        verify(followRelationRepository).save(relationCaptor.capture());
        assertThat(relationCaptor.getValue().getFollowerUserId()).isEqualTo(followerId);
        assertThat(relationCaptor.getValue().getFolloweeUserId()).isEqualTo(followeeId);
        verify(followEventPublisher).publishFollowCreated(relationCaptor.getValue());
    }

    @Test
    void followUserRejectsSelfFollow() {
        UUID userId = UUID.randomUUID();

        assertThatThrownBy(() -> followService.followUser(userId.toString(), userId.toString()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("cannot follow themselves");
    }

    @Test
    void followUserRejectsDuplicateFollow() {
        UUID followerId = UUID.randomUUID();
        UUID followeeId = UUID.randomUUID();
        when(followRelationRepository.existsByFollowerUserIdAndFolloweeUserId(followerId, followeeId)).thenReturn(true);

        assertThatThrownBy(() -> followService.followUser(followerId.toString(), followeeId.toString()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void returnsCountsAndFollowingList() {
        UUID followerId = UUID.randomUUID();
        UUID followeeId = UUID.randomUUID();
        FollowRelation relation = new FollowRelation();
        relation.setFollowerUserId(followerId);
        relation.setFolloweeUserId(followeeId);

        when(followRelationRepository.countByFolloweeUserId(followeeId)).thenReturn(7L);
        when(followRelationRepository.countByFollowerUserId(followerId)).thenReturn(3L);
        when(followRelationRepository.findByFollowerUserIdOrderByCreatedAtDesc(followerId)).thenReturn(List.of(relation));

        assertThat(followService.getFollowersCount(followeeId.toString())).isEqualTo(7L);
        assertThat(followService.getFollowingCount(followerId.toString())).isEqualTo(3L);
        assertThat(followService.getFollowing(followerId.toString())).containsExactly(followeeId.toString());
    }
}
