package com.connectsphere.post.domain.repository;

import com.connectsphere.post.domain.entity.PostCommentEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostCommentRepository extends JpaRepository<PostCommentEntity, UUID> {

    long countByPostId(UUID postId);

    List<PostCommentEntity> findTop20ByPostIdOrderByCreatedAtDesc(UUID postId);

    void deleteByPostId(UUID postId);
}
