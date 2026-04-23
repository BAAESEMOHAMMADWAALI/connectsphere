package com.connectsphere.user.service;

import com.connectsphere.user.domain.entity.UserAccount;
import com.connectsphere.user.events.UserRegisteredEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class UserEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(UserEventPublisher.class);
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
            kafkaTemplate.send(USER_REGISTERED_TOPIC, userAccount.getId().toString(), objectMapper.writeValueAsString(event))
                    .whenComplete((result, exception) -> {
                        if (exception != null) {
                            log.warn("User registered event could not be published for user {}", userAccount.getId(), exception);
                        }
                    });
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to serialize user registration event", exception);
        } catch (RuntimeException exception) {
            log.warn("User registered event publish was skipped for user {}", userAccount.getId(), exception);
        }
    }
}
