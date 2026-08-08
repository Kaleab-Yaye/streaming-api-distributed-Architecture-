package com.adnakiwoch.platform.streaming_api.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration // i made sure spring boot looks at this class at start up
public class SecurityConfig {
  @Bean // the object returned form this methode will becomes a bean manged by spring
  public SecurityFilterChain securityFilterChain(
      HttpSecurity httpSecurity, JwtAuthenticationFilter jwtAuthenticationFilter)
      throws Exception { // HttpSecuirty Object lets us build the Filter chain in builder patter
    httpSecurity
        .sessionManagement(
            session ->
                session.sessionCreationPolicy(
                    SessionCreationPolicy
                        .STATELESS)) // this puts in to the list that we dont need cookies
        .csrf(
            csrf ->
                csrf.disable()) // since no cockies is bieing used no need fo tthe csrf protection
        // needed
        .authorizeHttpRequests(
            auth ->
                auth // this is how we build the requist chain to make sure that some parts are
                    // allow all some parts are allow all
                    .requestMatchers(
                        "/api/v1/health",
                        "/api/v1/user/new_user",
                        "/api/v1/user/sign_in",
                        "/actuator/**",
                        "api/hooks/tusd/upload",
                        "/api/hooks/encode/**",
                        "api/v1/player/vid/play",
                        "/api/v1/encoder/regist/new")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .addFilterBefore(
            jwtAuthenticationFilter,
            UsernamePasswordAuthenticationFilter
                .class); // we need all the normal steps that spring woudl take when the
    // requist is basic, like passowrd aoutentication
    return httpSecurity
        .build(); // the list is build and tomcat can now iterate over eatch filters to see what to
    // do with teh request
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationManager authenticationManager(
      AuthenticationConfiguration authenticationConfiguration) throws Exception {
    return authenticationConfiguration.getAuthenticationManager();
  }
}
