package com.adnakiwoch.platform.streaming_api.config.beans;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ObjectMapper;

@Configuration
public class BeanStoreInt {
  @Bean
  public ObjectMapper retObjectMapper() {
    return new ObjectMapper();
  }
}
