package com.adnakiwoch.platform.streaming_api.dto.request.hook.streaming_node;

import java.util.UUID;

public record SubmitCurrentFrameRequest (
        UUID watchId,
        Double currentFrame
) {
}
