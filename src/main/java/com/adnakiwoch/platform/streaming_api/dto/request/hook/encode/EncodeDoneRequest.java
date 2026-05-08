package com.adnakiwoch.platform.streaming_api.dto.request.hook.encode;

import java.util.UUID;

public record EncodeDoneRequest(
    UUID vidId, boolean rawDeleted, String finalLocation, Double length) {}
