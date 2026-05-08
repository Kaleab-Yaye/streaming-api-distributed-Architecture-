package com.adnakiwoch.platform.streaming_api.dto.request.upload;

import jakarta.validation.constraints.NotNull;

public record UploadVidRequest(@NotNull String name, @NotNull String about) {}
