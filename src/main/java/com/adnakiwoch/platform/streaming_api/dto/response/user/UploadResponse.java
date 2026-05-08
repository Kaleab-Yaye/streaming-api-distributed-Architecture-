package com.adnakiwoch.platform.streaming_api.dto.response.user;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record UploadResponse(@NotNull UUID vid_id) {}
