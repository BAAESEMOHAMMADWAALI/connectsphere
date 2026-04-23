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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PostEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(PostEventPublisher.class);

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
            kafkaTemplate.send(topic, key, objectMapper.writeValueAsString(payload))
                    .whenComplete((result, exception) -> {
                        if (exception != null) {
                            log.warn("Kafka event could not be published to topic {} with key {}", topic, key, exception);
                        }
                    });
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to serialize Kafka event", exception);
        } catch (RuntimeException exception) {
            log.warn("Kafka event publish was skipped for topic {} with key {}", topic, key, exception);
        }
    }
}
