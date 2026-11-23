package com.pod.back.storage;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
@EnableConfigurationProperties(R2Properties.class)
public class R2ClientConfig {

    // R2 est compatible S3 : region "auto" (convention Cloudflare, ignorée pour le routage mais
    // requise par le SDK pour la signature des requêtes) + endpoint explicite vers le compte R2.
    @Bean
    public S3Client r2Client(R2Properties properties) {
        return S3Client.builder()
                .region(Region.of("auto"))
                .endpointOverride(URI.create("https://" + properties.accountId() + ".r2.cloudflarestorage.com"))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(properties.accessKeyId(), properties.secretAccessKey())))
                .build();
    }
}
