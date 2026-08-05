package com.adnakiwoch.platform.streaming_api.web.controller.player;

import com.adnakiwoch.platform.streaming_api.service.vid.VidService;
import com.adnakiwoch.platform.streaming_api.service.vid.WatchService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/player")
public class PlayerController {
  private final VidService vidService;
  private final WatchService watchService;

  public PlayerController(VidService vidService, WatchService watchService) {
    this.vidService = vidService;
    this.watchService = watchService;
  }

  @GetMapping("/auth/vid")
  @PreAuthorize("ACTIVE")
  public ResponseEntity<HttpStatus> authVidRequest(
      HttpServletRequest request, @AuthenticationPrincipal UserDetails userDetails) {
    log.info("auth trigered");
    log.info(
        "recived the uri {}, and current frame reqeust of {}, and the autorization token of {}",
        request.getHeader("request-uri"),
        request.getHeader("current-frame"),
        request.getHeader("Authorization"));

    return vidService.watchVidAuthOrchestrator(request, userDetails);
  }
}
