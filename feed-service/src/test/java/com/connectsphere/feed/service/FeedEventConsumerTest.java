package com.connectsphere.feed.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class FeedEventConsumerTest {

    private final FeedService feedService = mock(FeedService.class);
    private final FeedEventConsumer consumer = new FeedEventConsumer(feedService, new ObjectMapper());

    @Test
    void invalidatesFollowerFeedFromFollowEvent() {
        String followerUserId = UUID.randomUUID().toString();
        String payload = """
                {
                  "fromUserId": "%s",
                  "toUserId": "%s"
                }
                """.formatted(followerUserId, UUID.randomUUID());

        consumer.invalidateFeedCacheForFollow(payload);

        verify(feedService).invalidateFeedForUser(followerUserId);
    }

    @Test
    void invalidatesAuthorAndFollowerFeedsFromPostCreatedEvent() {
        String authorUserId = UUID.randomUUID().toString();
        String payload = """
                {
                  "postId": "%s",
                  "authorUserId": "%s"
                }
                """.formatted(UUID.randomUUID(), authorUserId);

        consumer.invalidateFeedCacheForNewPost(payload);

        verify(feedService).invalidateFeedsForPostAuthor(authorUserId);
    }
}
