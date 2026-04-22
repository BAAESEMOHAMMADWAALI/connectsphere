package com.connectsphere.feed;

import com.connectsphere.feed.config.DownstreamServicesProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(DownstreamServicesProperties.class)
public class FeedServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FeedServiceApplication.class, args);
    }
}

