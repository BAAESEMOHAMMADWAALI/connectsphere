package com.connectsphere.follow.service;

import com.connectsphere.follow.domain.entity.FollowRelation;
import com.connectsphere.follow.events.UserFollowedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class FollowEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(FollowEventPublisher.class);
    private static final String USER_FOLLOWED_TOPIC = "user-followed";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public FollowEventPublisher(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishFollowCreated(FollowRelation followRelation) {
        UserFollowedEvent event = new UserFollowedEvent(
                followRelation.getFollowerUserId().toString(),
                followRelation.getFolloweeUserId().toString(),
                followRelation.getCreatedAt()
        );

        try {
            kafkaTemplate.send(USER_FOLLOWED_TOPIC, followRelation.getFollowerUserId().toString(), objectMapper.writeValueAsString(event))
                    .whenComplete((result, exception) -> {
                        if (exception != null) {
                            log.warn("Follow event could not be published for follower {}", followRelation.getFollowerUserId(), exception);
                        }
                    });
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to serialize follow event", exception);
        } catch (RuntimeException exception) {
            log.warn("Follow event publish was skipped for follower {}", followRelation.getFollowerUserId(), exception);
        }
    }
}
