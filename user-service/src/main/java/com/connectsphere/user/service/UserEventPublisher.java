package com.connectsphere.user.service;

import com.connectsphere.user.domain.entity.UserAccount;
import com.connectsphere.user.events.UserRegisteredEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class UserEventPublisher {

    private static final String USER_REGISTERED_TOPIC = "user-registered";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public UserEventPublisher(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishUserRegistered(UserAccount userAccount) {
        UserRegisteredEvent event = new UserRegisteredEvent(
                userAccount.getId().toString(),
                userAccount.getEmail(),
                userAccount.getDisplayName(),
                userAccount.getCreatedAt()
        );

        try {
            kafkaTemplate.send(USER_REGISTERED_TOPIC, userAccount.getId().toString(), objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to serialize user registration event", exception);
        }
    }
}

