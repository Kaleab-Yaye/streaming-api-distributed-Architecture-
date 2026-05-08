package com.adnakiwoch.platform.streaming_api.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("platform.billing")
public record BillingProperties(@NotBlank String apiKey, int trailPeriodDays) {}
