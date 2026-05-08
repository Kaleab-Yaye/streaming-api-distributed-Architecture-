package com.adnakiwoch.platform.streaming_api.dto.request.hook.encode;

import java.util.UUID;

public record EncodeFailedRequest(
    UUID vidId,
    boolean notVid,
    boolean brokenVid,
    boolean issueNotSpecified,
    boolean fileDeleted) {}
