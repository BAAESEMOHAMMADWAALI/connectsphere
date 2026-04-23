package com.connectsphere.follow.api;

import com.connectsphere.follow.api.dto.FollowRequest;
import com.connectsphere.follow.api.dto.FollowResponse;
import com.connectsphere.follow.service.FollowService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/follows", "/api/v1/follows"})
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

    @PostMapping("/follow")
    @ResponseStatus(HttpStatus.CREATED)
    public FollowResponse followUserFromBody(@Valid @RequestBody FollowRequest request) {
        return followService.followUser(request.normalizedFollowerId(), request.resolvedFollowingId());
    }

    @DeleteMapping("/{targetUserId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unfollowUser(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String targetUserId
    ) {
        followService.unfollowUser(userId, targetUserId);
    }

    @DeleteMapping("/unfollow")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unfollowUserFromParams(
            @RequestParam String followerId,
            @RequestParam(required = false) String followingId,
            @RequestParam(required = false) String followeeId
    ) {
        followService.unfollowUser(followerId, followService.resolveFolloweeId(followingId, followeeId));
    }

    @GetMapping("/{userId}/followers")
    public List<String> getFollowers(@PathVariable String userId) {
        return followService.getFollowers(userId);
    }

    @GetMapping("/{userId}/following")
    public List<String> getFollowing(@PathVariable String userId) {
        return followService.getFollowing(userId);
    }

    @GetMapping("/followers/{userId}")
    public long getFollowersCount(@PathVariable String userId) {
        return followService.getFollowersCount(userId);
    }

    @GetMapping("/following/{userId}")
    public long getFollowingCount(@PathVariable String userId) {
        return followService.getFollowingCount(userId);
    }

    @GetMapping("/list/{userId}")
    public List<String> getFollowingList(@PathVariable String userId) {
        return followService.getFollowing(userId);
    }
}
