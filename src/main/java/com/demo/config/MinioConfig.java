package com.demo.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {

    @Value("${minio.url}")
    private String minioUrl;

    @Value("${minio.access-key}")
    private String accessKey;

    @Value("${minio.secret-key}")
    private String secretKey;

    @Bean
    public MinioClient minioClient() {
        String endpoint = minioUrl;
        if (endpoint != null && endpoint.contains("<IP_Ubuntu>")) {
            endpoint = endpoint.replace("<IP_Ubuntu>", "192.168.102.10");
        }
        if (endpoint != null && !endpoint.startsWith("http://") && !endpoint.startsWith("https://")) {
            endpoint = "http://" + endpoint;
        }
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }
}
