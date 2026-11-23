package com.pod.back.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "r2")
public record R2Properties(
        String accountId,
        String accessKeyId,
        String secretAccessKey,
        String bucket,
        // URL publique servant les objets du bucket (r2.dev ou domaine custom), sans slash final.
        String publicBaseUrl
) {
}
