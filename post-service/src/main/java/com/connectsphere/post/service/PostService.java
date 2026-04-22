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
        PostEntity postEntity = new PostEntity();
        postEntity.setAuthorUserId(parseUuid(authorUserId, "author user id"));
        postEntity.setCaption(request.caption().trim());
        postEntity.setMediaUrl(request.mediaUrl().trim());

        PostEntity savedPost = postRepository.save(postEntity);
        postEventPublisher.publishPostCreated(savedPost);
        return mapToResponse(savedPost);
    }

    @Transactional(readOnly = true)
    public PostResponse getPost(String postId) {
        return mapToResponse(findPost(postId));
    }

    @Transactional(readOnly = true)
    public List<PostResponse> getPostsByAuthors(List<String> authorIds, int limit) {
        if (authorIds == null || authorIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<UUID> authorUserIds = authorIds.stream()
                .map(authorId -> parseUuid(authorId, "author id"))
                .toList();

        return postRepository.findByAuthorUserIdInOrderByCreatedAtDesc(authorUserIds, PageRequest.of(0, limit)).stream()
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

    private PostEntity findPost(String postId) {
        return postRepository.findById(parseUuid(postId, "post id"))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
    }

    private UUID parseUuid(String value, String fieldName) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid " + fieldName, exception);
        }
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
                postEntity.getCreatedAt(),
                postLikeRepository.countByPostId(postEntity.getId()),
                postCommentRepository.countByPostId(postEntity.getId()),
                comments
        );
    }
}

