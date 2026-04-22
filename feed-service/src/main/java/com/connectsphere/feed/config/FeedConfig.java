package com.connectsphere.feed.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class FeedConfig {

    @Bean
    RestClient followServiceRestClient(DownstreamServicesProperties properties) {
        return RestClient.builder()
                .baseUrl(properties.followServiceBaseUrl())
                .build();
    }

    @Bean
    RestClient postServiceRestClient(DownstreamServicesProperties properties) {
        return RestClient.builder()
                .baseUrl(properties.postServiceBaseUrl())
                .build();
    }
}

