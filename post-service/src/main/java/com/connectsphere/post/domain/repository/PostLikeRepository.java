package com.connectsphere.post.domain.repository;

import com.connectsphere.post.domain.entity.PostLikeEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostLikeRepository extends JpaRepository<PostLikeEntity, UUID> {

    long countByPostId(UUID postId);

    boolean existsByPostIdAndUserId(UUID postId, UUID userId);

    void deleteByPostId(UUID postId);
}
