package com.adnakiwoch.platform.streaming_api.web.controller.user;

import com.adnakiwoch.platform.streaming_api.dto.request.registor.RegisterNewUserReq;
import com.adnakiwoch.platform.streaming_api.dto.request.security.AuthRequest;
import com.adnakiwoch.platform.streaming_api.dto.response.security.AuthResponse;
import com.adnakiwoch.platform.streaming_api.service.security.AuthService;
import com.adnakiwoch.platform.streaming_api.service.user.UserService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
@Slf4j
public class BasicUserController {
  private final UserService userService;
  private final AuthService authService;
  private final MeterRegistry meterRegistry;
  private final Counter counterForNewUser;
  private final Counter counterForLogInAttempt;

  public BasicUserController(
      UserService userService, AuthService authService, MeterRegistry meterRegistry) {
    this.userService = userService;
    this.authService = authService;
    this.meterRegistry = meterRegistry;
    this.counterForNewUser =
        Counter.builder("new_user_attempt")
            .tag("Basic_User", "New_User")
            .description("counts total attempts to register")
            .register(meterRegistry);

    this.counterForLogInAttempt =
        Counter.builder("Login_attempt")
            .tag("Basic_User", "Log_In")
            .description("counts total attempts to log in")
            .register(meterRegistry);
  }

  @PostMapping("/new_user")
  @ResponseStatus(HttpStatus.CREATED)
  public void registerNewUser(@Validated @RequestBody RegisterNewUserReq registerNewUserReq) {
    counterForNewUser.increment();
    userService.register(
        registerNewUserReq.name(), registerNewUserReq.email(), registerNewUserReq.password());
  }

  @PostMapping("/sign_in")
  public ResponseEntity<AuthResponse> logIn(@Validated @RequestBody AuthRequest authRequest) {
    log.info(" log in hit with the info {}", authRequest);
    counterForLogInAttempt.increment();
    return ResponseEntity.status(HttpStatus.ACCEPTED)
        .body(authService.login(authRequest.email(), authRequest.password()));
  }
}
