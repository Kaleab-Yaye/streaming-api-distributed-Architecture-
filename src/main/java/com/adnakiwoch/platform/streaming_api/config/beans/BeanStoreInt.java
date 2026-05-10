package com.adnakiwoch.platform.streaming_api.config.beans;

import java.net.URI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import tools.jackson.databind.ObjectMapper;

@Configuration
public class BeanStoreInt {

  private final String s3_uri = System.getenv("S3_URI");
  ;

  private final String s3AccessKey = System.getenv("S3_ACCESS_KEY");

  private final String s3SecretAccessKey = System.getenv("S3_SECRET_ACCESS_KEY");
  ;

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
