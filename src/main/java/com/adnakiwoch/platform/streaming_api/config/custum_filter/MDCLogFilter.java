package com.adnakiwoch.platform.streaming_api.config.custum_filter;

import jakarta.servlet.*;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Component
public class MDCLogFilter implements Filter {

  String reqId = UUID.randomUUID().toString();

  @Override
  public void doFilter(ServletRequest var1, ServletResponse var2, FilterChain var3)
      throws IOException, ServletException {
    try {

      MDC.put("reqId", reqId);
      var3.doFilter(var1, var2);
    } finally {
      MDC.clear();
    }
  }
}
