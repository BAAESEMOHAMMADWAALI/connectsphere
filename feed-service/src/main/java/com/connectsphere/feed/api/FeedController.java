package com.connectsphere.feed.api;

import com.connectsphere.feed.api.dto.FeedResponse;
import com.connectsphere.feed.service.FeedService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/feed", "/api/v1/feed"})
public class FeedController {

    private final FeedService feedService;

    public FeedController(FeedService feedService) {
        this.feedService = feedService;
    }

    @GetMapping("/home")
    public FeedResponse getHomeFeed(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(required = false) Integer limit,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Integer size
    ) {
        return feedService.getHomeFeed(userId, page, resolveSize(limit, size));
    }

    @GetMapping("/{userId}")
    public FeedResponse getFeedForUser(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return feedService.getHomeFeed(userId, page, size);
    }

    private int resolveSize(Integer limit, Integer size) {
        if (size != null) {
            return size;
        }
        return limit == null ? 20 : limit;
    }
}
