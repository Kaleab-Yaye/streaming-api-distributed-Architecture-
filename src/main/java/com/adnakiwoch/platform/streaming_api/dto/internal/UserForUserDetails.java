package com.adnakiwoch.platform.streaming_api.dto.internal;

import java.util.UUID;

public record UserForUserDetails(UUID id, String password, Boolean status, Boolean isStudio) {}
