package com.connectsphere.post.service;

import com.connectsphere.post.api.dto.AddCommentRequest;
import com.connectsphere.post.api.dto.CommentResponse;
import com.connectsphere.post.api.dto.CreatePostRequest;
import com.connectsphere.post.api.dto.PostResponse;
import com.connectsphere.post.domain.entity.PostCommentEntity;
import com.connectsphere.post.domain.entity.PostEntity;
import com.connectsphere.post.domain.entity.PostLikeEntity;
import com.connectsphere.post.domain.repository.PostCommentRepository;
import com.connectsphere.post.domain.repository.PostLikeRepository;
import com.connectsphere.post.domain.repository.PostRepository;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final PostCommentRepository postCommentRepository;
    private final PostEventPublisher postEventPublisher;

    public PostService(
            PostRepository postRepository,
            PostLikeRepository postLikeRepository,
            PostCommentRepository postCommentRepository,
            PostEventPublisher postEventPublisher
    ) {
        this.postRepository = postRepository;
        this.postLikeRepository = postLikeRepository;
        this.postCommentRepository = postCommentRepository;
        this.postEventPublisher = postEventPublisher;
    }

    @Transactional
    public PostResponse createPost(String authorUserId, CreatePostRequest request) {
        String effectiveAuthorUserId = StringUtils.hasText(authorUserId) ? authorUserId : request.userId();
        PostEntity postEntity = new PostEntity();
        postEntity.setAuthorUserId(parseUuid(effectiveAuthorUserId, "author user id"));
        postEntity.setCaption(request.normalizedCaption());
        postEntity.setMediaUrl(request.resolvedMediaUrl());

        PostEntity savedPost = postRepository.save(postEntity);
        postEventPublisher.publishPostCreated(savedPost);
        return mapToResponse(savedPost);
    }

    @Transactional(readOnly = true)
    public PostResponse getPost(String postId) {
        return mapToResponse(findPost(postId));
    }

    @Transactional(readOnly = true)
    public List<PostResponse> getAllPosts(int limit) {
        return postRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, normalizeLimit(limit))).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PostResponse> getPostsByUser(String authorUserId, int limit) {
        return postRepository.findByAuthorUserIdOrderByCreatedAtDesc(
                        parseUuid(authorUserId, "author user id"),
                        PageRequest.of(0, normalizeLimit(limit))
                ).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PostResponse> getPostsByAuthors(List<String> authorIds, int limit) {
        if (authorIds == null || authorIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<UUID> authorUserIds = authorIds.stream()
                .map(authorId -> parseUuid(authorId, "author id"))
                .toList();

        return postRepository.findByAuthorUserIdInOrderByCreatedAtDesc(authorUserIds, PageRequest.of(0, normalizeLimit(limit))).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public PostResponse likePost(String postId, String actorUserId) {
        PostEntity postEntity = findPost(postId);
        UUID actorId = parseUuid(actorUserId, "actor user id");

        if (!postLikeRepository.existsByPostIdAndUserId(postEntity.getId(), actorId)) {
            PostLikeEntity likeEntity = new PostLikeEntity();
            likeEntity.setPostId(postEntity.getId());
            likeEntity.setUserId(actorId);
            postLikeRepository.save(likeEntity);
            postEventPublisher.publishPostLiked(postEntity, actorId);
        }

        return mapToResponse(postEntity);
    }

    @Transactional
    public PostResponse addComment(String postId, String actorUserId, AddCommentRequest request) {
        PostEntity postEntity = findPost(postId);

        PostCommentEntity commentEntity = new PostCommentEntity();
        commentEntity.setPostId(postEntity.getId());
        commentEntity.setUserId(parseUuid(actorUserId, "actor user id"));
        commentEntity.setText(request.text().trim());

        PostCommentEntity savedComment = postCommentRepository.save(commentEntity);
        postEventPublisher.publishCommentCreated(postEntity, savedComment);
        return mapToResponse(postEntity);
    }

    @Transactional
    public void deletePost(String postId, String actorUserId) {
        PostEntity postEntity = findPost(postId);
        if (StringUtils.hasText(actorUserId)) {
            UUID actorId = parseUuid(actorUserId, "actor user id");
            if (!postEntity.getAuthorUserId().equals(actorId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the post owner can delete this post");
            }
        }

        postLikeRepository.deleteByPostId(postEntity.getId());
        postCommentRepository.deleteByPostId(postEntity.getId());
        postRepository.delete(postEntity);
    }

    private PostEntity findPost(String postId) {
        return postRepository.findById(parseUuid(postId, "post id"))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
    }

    private UUID parseUuid(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing " + fieldName);
        }
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid " + fieldName, exception);
        }
    }

    private int normalizeLimit(int limit) {
        if (limit < 1) {
            return 20;
        }
        return Math.min(limit, 100);
    }

    private PostResponse mapToResponse(PostEntity postEntity) {
        List<CommentResponse> comments = postCommentRepository.findTop20ByPostIdOrderByCreatedAtDesc(postEntity.getId()).stream()
                .map(comment -> new CommentResponse(
                        comment.getId().toString(),
                        comment.getUserId().toString(),
                        comment.getText(),
                        comment.getCreatedAt()
                ))
                .toList();

        return new PostResponse(
                postEntity.getId().toString(),
                postEntity.getAuthorUserId().toString(),
                postEntity.getCaption(),
                postEntity.getMediaUrl(),
                postEntity.getMediaUrl(),
                postEntity.getCreatedAt(),
                postLikeRepository.countByPostId(postEntity.getId()),
                postCommentRepository.countByPostId(postEntity.getId()),
                comments
        );
    }
}
