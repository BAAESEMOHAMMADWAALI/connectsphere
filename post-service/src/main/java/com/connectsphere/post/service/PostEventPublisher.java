package com.connectsphere.post.service;

import com.connectsphere.post.domain.entity.PostCommentEntity;
import com.connectsphere.post.domain.entity.PostEntity;
import com.connectsphere.post.events.CommentCreatedEvent;
import com.connectsphere.post.events.PostCreatedEvent;
import com.connectsphere.post.events.PostLikedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.UUID;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PostEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public PostEventPublisher(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishPostCreated(PostEntity postEntity) {
        publish("post-created", postEntity.getId().toString(), new PostCreatedEvent(
                postEntity.getId().toString(),
                postEntity.getAuthorUserId().toString(),
                postEntity.getCaption(),
                postEntity.getMediaUrl(),
                postEntity.getCreatedAt()
        ));
    }

    public void publishPostLiked(PostEntity postEntity, UUID actorUserId) {
        publish("post-liked", postEntity.getId().toString(), new PostLikedEvent(
                postEntity.getId().toString(),
                actorUserId.toString(),
                postEntity.getAuthorUserId().toString(),
                Instant.now()
        ));
    }

    public void publishCommentCreated(PostEntity postEntity, PostCommentEntity commentEntity) {
        publish("comment-created", postEntity.getId().toString(), new CommentCreatedEvent(
                postEntity.getId().toString(),
                commentEntity.getId().toString(),
                commentEntity.getUserId().toString(),
                postEntity.getAuthorUserId().toString(),
                commentEntity.getText(),
                commentEntity.getCreatedAt()
        ));
    }

    private void publish(String topic, String key, Object payload) {
        try {
            kafkaTemplate.send(topic, key, objectMapper.writeValueAsString(payload));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to serialize Kafka event", exception);
        }
    }
}

