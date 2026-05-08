package com.adnakiwoch.platform.streaming_api.dto.request.registor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterNewUserReq(
    @NotBlank(message = "name should be valid")
        @Size(message = "the name is too short", min = 3, max = 32)
        String name,
    @NotBlank(message = "Email is needed") @Email(message = "not formal email addresss")
        String email,
    @NotBlank(message = "passsword is needed")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9])\\S{8,20}$")
        String password) {}
