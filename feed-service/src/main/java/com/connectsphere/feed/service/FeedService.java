package com.connectsphere.feed.service;

import com.connectsphere.feed.api.dto.FeedItemResponse;
import com.connectsphere.feed.api.dto.FeedResponse;
import com.connectsphere.feed.api.dto.RemotePostResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class FeedService {

    private static final Duration FEED_CACHE_TTL = Duration.ofMinutes(5);

    private final RestClient followServiceRestClient;
    private final RestClient postServiceRestClient;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public FeedService(
            @Qualifier("followServiceRestClient") RestClient followServiceRestClient,
            @Qualifier("postServiceRestClient") RestClient postServiceRestClient,
            StringRedisTemplate stringRedisTemplate,
            ObjectMapper objectMapper
    ) {
        this.followServiceRestClient = followServiceRestClient;
        this.postServiceRestClient = postServiceRestClient;
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }

    public FeedResponse getHomeFeed(String userId, int limit) {
        String cacheKey = "feed:%s:%d".formatted(userId, limit);
        String cachedValue = stringRedisTemplate.opsForValue().get(cacheKey);
        if (cachedValue != null) {
            return new FeedResponse(userId, readCachedItems(cachedValue), true);
        }

        List<String> following = followServiceRestClient.get()
                .uri("/api/v1/follows/{userId}/following", userId)
                .retrieve()
                .body(new ParameterizedTypeReference<List<String>>() {
                });

        LinkedHashSet<String> authorIds = new LinkedHashSet<>();
        authorIds.add(userId);
        if (following != null) {
            authorIds.addAll(following);
        }

        List<FeedItemResponse> items = fetchPosts(new ArrayList<>(authorIds), limit);
        writeCachedItems(cacheKey, items);
        return new FeedResponse(userId, items, false);
    }

    public void invalidateAllFeeds() {
        var keys = stringRedisTemplate.keys("feed:*");
        if (keys != null && !keys.isEmpty()) {
            stringRedisTemplate.delete(keys);
        }
    }

    private List<FeedItemResponse> fetchPosts(List<String> authorIds, int limit) {
        if (authorIds.isEmpty()) {
            return List.of();
        }

        List<RemotePostResponse> posts = postServiceRestClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder.path("/api/v1/posts/by-authors");
                    authorIds.forEach(authorId -> builder.queryParam("authorIds", authorId));
                    builder.queryParam("limit", limit);
                    return builder.build();
                })
                .retrieve()
                .body(new ParameterizedTypeReference<List<RemotePostResponse>>() {
                });

        if (posts == null) {
            return List.of();
        }

        return posts.stream()
                .map(post -> new FeedItemResponse(
                        post.id(),
                        post.authorUserId(),
                        post.caption(),
                        post.mediaUrl(),
                        post.createdAt(),
                        post.likeCount(),
                        post.commentCount()
                ))
                .toList();
    }

    private List<FeedItemResponse> readCachedItems(String cachedValue) {
        try {
            return objectMapper.readValue(cachedValue, new TypeReference<List<FeedItemResponse>>() {
            });
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to deserialize cached feed", exception);
        }
    }

    private void writeCachedItems(String cacheKey, List<FeedItemResponse> items) {
        try {
            stringRedisTemplate.opsForValue().set(cacheKey, objectMapper.writeValueAsString(items), FEED_CACHE_TTL);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to serialize feed cache", exception);
        }
    }
}
