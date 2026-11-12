package com.pod.back.printful.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "printful.api")
public record PrintfulProperties(
        String baseUrl,
        String key,
        int connectTimeoutMs,
        int readTimeoutMs
) {
}
