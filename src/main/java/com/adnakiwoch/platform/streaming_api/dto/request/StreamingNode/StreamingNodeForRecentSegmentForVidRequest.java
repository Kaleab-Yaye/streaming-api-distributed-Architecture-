package com.adnakiwoch.platform.streaming_api.dto.request.StreamingNode;

public record StreamingNodeForRecentSegmentForVidRequest (
        String encodedLocation,
        Double recentFrame
) {
}
