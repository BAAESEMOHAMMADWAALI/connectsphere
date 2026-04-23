package com.connectsphere.post.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    NewTopic postCreatedTopic() {
        return TopicBuilder.name("post-created")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    NewTopic postLikedTopic() {
        return TopicBuilder.name("post-liked")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    NewTopic commentCreatedTopic() {
        return TopicBuilder.name("comment-created")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
