package com.sketchnotes.project_service.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "spring.aws.s3")
public class S3Properties {
    private String accessKey;
    private String secretKey;
    private String bucketName;
    private String region;
    private long presignExpiration;
}
