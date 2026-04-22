package com.connectsphere.feed.api;

import com.connectsphere.feed.api.dto.FeedResponse;
import com.connectsphere.feed.service.FeedService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/feed")
public class FeedController {

    private final FeedService feedService;

    public FeedController(FeedService feedService) {
        this.feedService = feedService;
    }

    @GetMapping("/home")
    public FeedResponse getHomeFeed(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(defaultValue = "20") int limit
    ) {
        return feedService.getHomeFeed(userId, Math.min(limit, 50));
    }
}

