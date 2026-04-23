package com.connectsphere.post.api;

import com.connectsphere.post.api.dto.AddCommentRequest;
import com.connectsphere.post.api.dto.CreatePostRequest;
import com.connectsphere.post.api.dto.PostResponse;
import com.connectsphere.post.service.PostService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/posts", "/api/v1/posts"})
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PostResponse createPost(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @Valid @RequestBody CreatePostRequest request
    ) {
        return postService.createPost(userId, request);
    }

    @GetMapping
    public List<PostResponse> getAllPosts(@RequestParam(defaultValue = "50") int limit) {
        return postService.getAllPosts(limit);
    }

    @GetMapping("/{postId}")
    public PostResponse getPost(@PathVariable String postId) {
        return postService.getPost(postId);
    }

    @GetMapping("/user/{userId}")
    public List<PostResponse> getPostsByUser(
            @PathVariable String userId,
            @RequestParam(defaultValue = "50") int limit
    ) {
        return postService.getPostsByUser(userId, limit);
    }

    @GetMapping("/by-authors")
    public List<PostResponse> getPostsByAuthors(
            @RequestParam List<String> authorIds,
            @RequestParam(defaultValue = "20") int limit
    ) {
        return postService.getPostsByAuthors(authorIds, Math.min(limit, 50));
    }

    @PostMapping("/{postId}/likes")
    public PostResponse likePost(
            @PathVariable String postId,
            @RequestHeader("X-User-Id") String userId
    ) {
        return postService.likePost(postId, userId);
    }

    @PostMapping("/{postId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public PostResponse addComment(
            @PathVariable String postId,
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody AddCommentRequest request
    ) {
        return postService.addComment(postId, userId, request);
    }

    @DeleteMapping("/{postId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePost(
            @PathVariable String postId,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestParam(value = "userId", required = false) String requestUserId
    ) {
        postService.deletePost(postId, userId == null ? requestUserId : userId);
    }
}
