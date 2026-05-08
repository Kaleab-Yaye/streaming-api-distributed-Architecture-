package com.adnakiwoch.platform.streaming_api.dto.header;

import java.util.UUID;

public record GetVidHeader(UUID vidId, Double currentFrame) {}
