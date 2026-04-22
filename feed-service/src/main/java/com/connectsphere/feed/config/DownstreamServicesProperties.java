package com.connectsphere.feed.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "services")
public record DownstreamServicesProperties(
        String postServiceBaseUrl,
        String followServiceBaseUrl
) {
}

