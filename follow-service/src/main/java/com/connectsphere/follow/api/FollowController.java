package com.connectsphere.follow.api;

import com.connectsphere.follow.api.dto.FollowResponse;
import com.connectsphere.follow.service.FollowService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/follows")
public class FollowController {

    private final FollowService followService;

    public FollowController(FollowService followService) {
        this.followService = followService;
    }

    @PostMapping("/{targetUserId}")
    @ResponseStatus(HttpStatus.CREATED)
    public FollowResponse followUser(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String targetUserId
    ) {
        return followService.followUser(userId, targetUserId);
    }

    @DeleteMapping("/{targetUserId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unfollowUser(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String targetUserId
    ) {
        followService.unfollowUser(userId, targetUserId);
    }

    @GetMapping("/{userId}/followers")
    public List<String> getFollowers(@PathVariable String userId) {
        return followService.getFollowers(userId);
    }

    @GetMapping("/{userId}/following")
    public List<String> getFollowing(@PathVariable String userId) {
        return followService.getFollowing(userId);
    }
}

