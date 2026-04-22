package com.connectsphere.post.domain.repository;

import com.connectsphere.post.domain.entity.PostEntity;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<PostEntity, UUID> {

    List<PostEntity> findByAuthorUserIdInOrderByCreatedAtDesc(Collection<UUID> authorUserIds, Pageable pageable);
}

