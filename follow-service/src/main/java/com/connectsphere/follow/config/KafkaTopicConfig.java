package com.connectsphere.follow.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    NewTopic userFollowedTopic() {
        return TopicBuilder.name("user-followed")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
