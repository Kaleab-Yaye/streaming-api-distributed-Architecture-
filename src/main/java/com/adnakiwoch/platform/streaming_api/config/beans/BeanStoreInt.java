package com.adnakiwoch.platform.streaming_api.config.beans;

import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import tools.jackson.databind.ObjectMapper;

@Configuration
public class BeanStoreInt {
  @Value("${s3.MINIO_URI}")
  private String s3_uri;

  @Value("${s3.MINIO_ROOT_USER}")
  private String s3AccessKey;

  @Value("${s3.MINIO_ROOT_PASSWORD}")
  private String s3SecretAccessKey;

  @Bean
  public ObjectMapper retObjectMapper() {
    return new ObjectMapper();
  }

  @Bean
  public S3Client retS3Client() {

    return S3Client.builder()
        .endpointOverride(URI.create(s3_uri))
        .credentialsProvider(
            StaticCredentialsProvider.create(
                AwsBasicCredentials.create(s3AccessKey, s3SecretAccessKey)))
        .region(
            Region
                .AF_SOUTH_1) // have almost no use with my miniio setup, but the builder demands it
        .forcePathStyle(true)
        .build();
  }
}
