package com.adnakiwoch.platform.streaming_api.dto.request.security;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AuthRequest(
    @NotBlank(message = "email is needed") String email,
    @NotNull(message = "password is needed") String password) {}
