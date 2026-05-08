package com.adnakiwoch.platform.streaming_api.service.security;

import com.adnakiwoch.platform.streaming_api.dto.response.security.AuthResponse;
import com.adnakiwoch.platform.streaming_api.exception.crypto.InternalCryptoException;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthService {
  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;
  private final MeterRegistry meterRegistry;
  private final Counter successfulLogInCounter;

  public AuthService(
      AuthenticationManager authenticationManager,
      JwtService jwtService,
      MeterRegistry meterRegistry) {
    this.authenticationManager = authenticationManager;
    this.jwtService = jwtService;
    this.meterRegistry = meterRegistry;
    this.successfulLogInCounter =
        Counter.builder("successful_log_in")
            .tag("AuthService", "Log_in")
            .description("counts successful log in attempts")
            .register(meterRegistry);
  }

  public AuthResponse login(String email, String password) {

    // kinda boiler plate just know this is the core of autentication before token is give
    // returen fully aoutenticated object and puts is in to the thread local

    try {
      Authentication authentication =
          authenticationManager.authenticate(
              new UsernamePasswordAuthenticationToken(email, password));

      // extract userDetials, since that is the generateToken takes as an arrguement
      UserDetails userDetails = (UserDetails) authentication.getPrincipal();

      MDC.put("userId", userDetails.getUsername());
      log.info("User Login Success");

      successfulLogInCounter.increment();
      return (new AuthResponse(jwtService.generateToken(userDetails)));

    } catch (AuthenticationException ex) {

      try {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");

        byte[] bitDigest = messageDigest.digest(email.getBytes(StandardCharsets.UTF_8));

        log.warn("User Login Failed. Email: " + HexFormat.of().formatHex(bitDigest));
      } catch (NoSuchAlgorithmException e) {
        throw new InternalCryptoException("Creating digest failed in logIn service", e);
      }

      throw ex;
    }
  }
}
