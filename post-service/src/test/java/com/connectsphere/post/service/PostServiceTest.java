package com.connectsphere.post.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.connectsphere.post.api.dto.CreatePostRequest;
import com.connectsphere.post.api.dto.PostResponse;
import com.connectsphere.post.domain.entity.PostEntity;
import com.connectsphere.post.domain.repository.PostCommentRepository;
import com.connectsphere.post.domain.repository.PostLikeRepository;
import com.connectsphere.post.domain.repository.PostRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostLikeRepository postLikeRepository;

    @Mock
    private PostCommentRepository postCommentRepository;

    @Mock
    private PostEventPublisher postEventPublisher;

    @InjectMocks
    private PostService postService;

    @Test
    void createPostAcceptsRequestUserIdAndImageUrl() {
        UUID authorId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();
        CreatePostRequest request = new CreatePostRequest(
                authorId.toString(),
                " My first post ",
                null,
                " https://img.com/pic.jpg "
        );

        when(postRepository.save(any(PostEntity.class))).thenAnswer(invocation -> {
            PostEntity postEntity = invocation.getArgument(0);
            ReflectionTestUtils.setField(postEntity, "id", postId);
            ReflectionTestUtils.setField(postEntity, "createdAt", Instant.parse("2026-04-23T09:00:00Z"));
            return postEntity;
        });
        when(postCommentRepository.findTop20ByPostIdOrderByCreatedAtDesc(postId)).thenReturn(List.of());

        PostResponse response = postService.createPost(null, request);

        assertThat(response.id()).isEqualTo(postId.toString());
        assertThat(response.authorUserId()).isEqualTo(authorId.toString());
        assertThat(response.caption()).isEqualTo("My first post");
        assertThat(response.mediaUrl()).isEqualTo("https://img.com/pic.jpg");
        assertThat(response.imageUrl()).isEqualTo("https://img.com/pic.jpg");

        ArgumentCaptor<PostEntity> postCaptor = ArgumentCaptor.forClass(PostEntity.class);
        verify(postRepository).save(postCaptor.capture());
        assertThat(postCaptor.getValue().getAuthorUserId()).isEqualTo(authorId);
        verify(postEventPublisher).publishPostCreated(postCaptor.getValue());
    }

    @Test
    void deletePostAllowsOwnerAndRemovesRelatedRows() {
        UUID authorId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();
        PostEntity postEntity = postEntity(postId, authorId);
        when(postRepository.findById(postId)).thenReturn(Optional.of(postEntity));

        postService.deletePost(postId.toString(), authorId.toString());

        verify(postLikeRepository).deleteByPostId(postId);
        verify(postCommentRepository).deleteByPostId(postId);
        verify(postRepository).delete(postEntity);
    }

    @Test
    void deletePostRejectsNonOwner() {
        UUID authorId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();
        when(postRepository.findById(postId)).thenReturn(Optional.of(postEntity(postId, authorId)));

        assertThatThrownBy(() -> postService.deletePost(postId.toString(), actorId.toString()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Only the post owner");
    }

    private PostEntity postEntity(UUID postId, UUID authorId) {
        PostEntity postEntity = new PostEntity();
        ReflectionTestUtils.setField(postEntity, "id", postId);
        ReflectionTestUtils.setField(postEntity, "createdAt", Instant.parse("2026-04-23T09:00:00Z"));
        postEntity.setAuthorUserId(authorId);
        postEntity.setCaption("Caption");
        postEntity.setMediaUrl("https://img.com/pic.jpg");
        return postEntity;
    }
}
