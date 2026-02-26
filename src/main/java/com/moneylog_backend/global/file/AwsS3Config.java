package com.moneylog_backend.global.file;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
@ConditionalOnProperty(prefix = "app.file.storage", name = "type", havingValue = "s3")
public class AwsS3Config {
    @Bean
    public S3Client s3Client(FileProperties fileProperties) {
        return S3Client.builder()
                       .region(Region.of(fileProperties.getS3().getRegion()))
                       .credentialsProvider(DefaultCredentialsProvider.create())
                       .build();
    }

    @Bean
    public S3Presigner s3Presigner(FileProperties fileProperties) {
        return S3Presigner.builder()
                          .region(Region.of(fileProperties.getS3().getRegion()))
                          .credentialsProvider(DefaultCredentialsProvider.create())
                          .build();
    }
}
