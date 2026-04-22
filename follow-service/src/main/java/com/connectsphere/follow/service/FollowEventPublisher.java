package com.connectsphere.follow.service;

import com.connectsphere.follow.domain.entity.FollowRelation;
import com.connectsphere.follow.events.UserFollowedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class FollowEventPublisher {

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
            kafkaTemplate.send("user-followed", followRelation.getFollowerUserId().toString(), objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to serialize follow event", exception);
        }
    }
}

