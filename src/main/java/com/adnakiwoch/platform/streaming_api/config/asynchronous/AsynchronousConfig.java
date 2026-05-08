package com.adnakiwoch.platform.streaming_api.config.asynchronous;

import java.util.concurrent.Executor;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsynchronousConfig implements AsyncConfigurer {

  @Bean
  public Executor emailAsyncExecutor() {

    ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
    threadPoolTaskExecutor.setCorePoolSize(10);
    threadPoolTaskExecutor.setMaxPoolSize(100);
    threadPoolTaskExecutor.setQueueCapacity(1000);
    threadPoolTaskExecutor.setKeepAliveSeconds(2);
    threadPoolTaskExecutor.initialize();

    return threadPoolTaskExecutor;
  }

  @Override
  public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
    return new AsyncExceptionHandler();
  }
}
