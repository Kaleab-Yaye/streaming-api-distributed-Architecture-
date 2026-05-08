package com.adnakiwoch.platform.streaming_api.web.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/health")
public class HealthController {
  @GetMapping("/check")
  public HealthResponse health() {
    return new HealthResponse("ok");
  }

  @GetMapping("/auth_check")
  public HealthResponse authCheck() {
    return new HealthResponse("ok");
  }
}
