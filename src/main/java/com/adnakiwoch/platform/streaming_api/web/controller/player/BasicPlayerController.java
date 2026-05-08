package com.adnakiwoch.platform.streaming_api.web.controller.player;

import com.adnakiwoch.platform.streaming_api.dto.response.PlayerBasicResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/player/vid/play")
public class BasicPlayerController {
  @GetMapping
  public ResponseEntity<PlayerBasicResponse> sampleVid() {
    return ResponseEntity.ok(new PlayerBasicResponse("test_vid3"));
  }
}
