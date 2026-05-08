package com.adnakiwoch.platform.streaming_api.config.asynchronous;

import java.lang.reflect.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;

@Slf4j
public class AsyncExceptionHandler implements AsyncUncaughtExceptionHandler {
  @Override
  public void handleUncaughtException(Throwable ex, Method method, Object... params) {
    log.warn(ex.getMessage());
    log.warn("methode name: {}", method);
    int paramCount = 1;
    for (Object object : params) {
      log.info("param " + paramCount + ":{}", object); // counts which param
      paramCount++;
    }
  }
}
