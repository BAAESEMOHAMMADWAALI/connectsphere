package com.connectsphere.feed.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.connectsphere.feed.api.dto.FeedResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class FeedServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private StringRedisTemplate redisTemplate;
    private ValueOperations<String, String> valueOperations;
    private RestClient.Builder followClientBuilder;
    private RestClient.Builder postClientBuilder;
    private MockRestServiceServer followServer;
    private MockRestServiceServer postServer;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        redisTemplate = mock(StringRedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        followClientBuilder = RestClient.builder().baseUrl("http://follow-service");
        postClientBuilder = RestClient.builder().baseUrl("http://post-service");
        followServer = MockRestServiceServer.bindTo(followClientBuilder).build();
        postServer = MockRestServiceServer.bindTo(postClientBuilder).build();
    }

    @Test
    void buildsFeedFromFollowedUsersAndWritesRedisCache() {
        String viewerUserId = UUID.randomUUID().toString();
        String followedUserId = UUID.randomUUID().toString();
        String cacheKey = "feed:user:%s:page:0:size:10".formatted(viewerUserId);

        when(valueOperations.get(cacheKey)).thenReturn(null);
        followServer.expect(requestTo("http://follow-service/api/v1/follows/%s/following".formatted(viewerUserId)))
                .andRespond(withSuccess("[\"%s\"]".formatted(followedUserId), MediaType.APPLICATION_JSON));
        postServer.expect(requestTo("http://post-service/api/v1/posts/by-authors?authorIds=%s&authorIds=%s&limit=10"
                        .formatted(viewerUserId, followedUserId)))
                .andRespond(withSuccess("""
                        [
                          {
                            "id": "post-1",
                            "authorUserId": "%s",
                            "caption": "Trip photo",
                            "mediaUrl": "https://img.com/pic.jpg",
                            "imageUrl": "https://img.com/pic.jpg",
                            "createdAt": "2026-04-23T09:00:00Z",
                            "likeCount": 2,
                            "commentCount": 1,
                            "comments": []
                          }
                        ]
                        """.formatted(followedUserId), MediaType.APPLICATION_JSON));

        FeedService feedService = feedService();
        FeedResponse response = feedService.getHomeFeed(viewerUserId, 0, 10);

        assertThat(response.cacheHit()).isFalse();
        assertThat(response.page()).isZero();
        assertThat(response.size()).isEqualTo(10);
        assertThat(response.items()).hasSize(1);
        assertThat(response.items().get(0).authorUserId()).isEqualTo(followedUserId);
        assertThat(response.items().get(0).imageUrl()).isEqualTo("https://img.com/pic.jpg");
        verify(valueOperations).set(eq(cacheKey), any(String.class), any(Duration.class));
        followServer.verify();
        postServer.verify();
    }

    @Test
    void returnsCachedFeedWhenRedisHasValue() {
        String viewerUserId = UUID.randomUUID().toString();
        String cacheKey = "feed:user:%s:page:1:size:5".formatted(viewerUserId);
        when(valueOperations.get(cacheKey)).thenReturn("""
                [
                  {
                    "id": "cached-post",
                    "authorUserId": "author",
                    "caption": "Cached",
                    "mediaUrl": "https://img.com/cached.jpg",
                    "imageUrl": "https://img.com/cached.jpg",
                    "createdAt": "2026-04-23T09:00:00Z",
                    "likeCount": 0,
                    "commentCount": 0
                  }
                ]
                """);

        FeedResponse response = feedService().getHomeFeed(viewerUserId, 1, 5);

        assertThat(response.cacheHit()).isTrue();
        assertThat(response.page()).isEqualTo(1);
        assertThat(response.size()).isEqualTo(5);
        assertThat(response.items()).extracting("id").containsExactly("cached-post");
    }

    @Test
    void invalidatesAllCachedPagesForUser() {
        String viewerUserId = UUID.randomUUID().toString();
        Set<String> keys = Set.of(
                "feed:user:%s:page:0:size:10".formatted(viewerUserId),
                "feed:user:%s:page:1:size:10".formatted(viewerUserId)
        );
        when(redisTemplate.keys("feed:user:%s:*".formatted(viewerUserId))).thenReturn(keys);

        feedService().invalidateFeedForUser(viewerUserId);

        verify(redisTemplate).delete(keys);
    }

    private FeedService feedService() {
        return new FeedService(
                followClientBuilder.build(),
                postClientBuilder.build(),
                redisTemplate,
                objectMapper
        );
    }
}
