package com.adnakiwoch.platform.streaming_api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("platform.cdn")
public record CdnProperties(String baseUrl, boolean privateStreamingEnabled) {}
