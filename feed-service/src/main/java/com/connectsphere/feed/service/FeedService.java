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
import java.util.Set;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

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
        return getHomeFeed(userId, 0, limit);
    }

    public FeedResponse getHomeFeed(String userId, int page, int size) {
        String normalizedUserId = normalizeUserId(userId);
        int normalizedPage = Math.max(page, 0);
        int normalizedSize = normalizeSize(size);
        String cacheKey = cacheKey(normalizedUserId, normalizedPage, normalizedSize);

        String cachedValue = stringRedisTemplate.opsForValue().get(cacheKey);
        if (cachedValue != null) {
            return new FeedResponse(normalizedUserId, normalizedPage, normalizedSize, readCachedItems(cachedValue), true);
        }

        List<String> following = followServiceRestClient.get()
                .uri("/api/v1/follows/{userId}/following", normalizedUserId)
                .retrieve()
                .body(new ParameterizedTypeReference<List<String>>() {
                });

        LinkedHashSet<String> authorIds = new LinkedHashSet<>();
        authorIds.add(normalizedUserId);
        if (following != null) {
            authorIds.addAll(following);
        }

        int requestLimit = Math.min((normalizedPage + 1) * normalizedSize, 200);
        List<FeedItemResponse> items = fetchPosts(new ArrayList<>(authorIds), requestLimit).stream()
                .skip((long) normalizedPage * normalizedSize)
                .limit(normalizedSize)
                .toList();
        writeCachedItems(cacheKey, items);
        return new FeedResponse(normalizedUserId, normalizedPage, normalizedSize, items, false);
    }

    public void invalidateAllFeeds() {
        var keys = stringRedisTemplate.keys("feed:*");
        if (keys != null && !keys.isEmpty()) {
            stringRedisTemplate.delete(keys);
        }
    }

    public void invalidateFeedForUser(String userId) {
        String normalizedUserId = normalizeUserId(userId);
        deleteKeys("feed:user:%s:*".formatted(normalizedUserId));
    }

    public void invalidateFeedsForPostAuthor(String authorUserId) {
        String normalizedAuthorUserId = normalizeUserId(authorUserId);
        invalidateFeedForUser(normalizedAuthorUserId);

        try {
            List<String> followers = followServiceRestClient.get()
                    .uri("/api/v1/follows/{userId}/followers", normalizedAuthorUserId)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<String>>() {
                    });
            if (followers != null) {
                followers.forEach(this::invalidateFeedForUser);
            }
        } catch (RuntimeException exception) {
            invalidateAllFeeds();
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
                        resolveMediaUrl(post),
                        resolveMediaUrl(post),
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

    private String cacheKey(String userId, int page, int size) {
        return "feed:user:%s:page:%d:size:%d".formatted(userId, page, size);
    }

    private void deleteKeys(String pattern) {
        Set<String> keys = stringRedisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            stringRedisTemplate.delete(keys);
        }
    }

    private String normalizeUserId(String userId) {
        if (!StringUtils.hasText(userId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userId is required");
        }
        return userId.trim();
    }

    private int normalizeSize(int size) {
        if (size < 1) {
            return 10;
        }
        return Math.min(size, 100);
    }

    private String resolveMediaUrl(RemotePostResponse post) {
        return StringUtils.hasText(post.mediaUrl()) ? post.mediaUrl() : post.imageUrl();
    }
}
