package com.adnakiwoch.platform.streaming_api.web.controller.Watch;

import com.adnakiwoch.platform.streaming_api.dto.request.vid.WatchVIdRequest;
import com.adnakiwoch.platform.streaming_api.dto.response.vid.GetAvailableVid;
import com.adnakiwoch.platform.streaming_api.dto.response.vid.WatchVidResponse;
import com.adnakiwoch.platform.streaming_api.service.vid.VidService;
import com.adnakiwoch.platform.streaming_api.service.vid.WatchService;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/watch")
public class WatchController {

  private final WatchService watchService;
  private final VidService vidService;

  public WatchController(WatchService watchService, VidService vidService) {
    this.watchService = watchService;
    this.vidService = vidService;
  }

  @GetMapping("/vid")
  @PreAuthorize("ACTIVE")
  public ResponseEntity<WatchVidResponse> watchHandler(
      @AuthenticationPrincipal UserDetails userDetails, @RequestParam String vidId) {
    WatchVIdRequest watchVIdRequest = new WatchVIdRequest(UUID.fromString(vidId));
    return vidService.watchVid(watchVIdRequest, userDetails);
  }

  @GetMapping("/page")
  @PreAuthorize("ACTIVE")
  public ResponseEntity<GetAvailableVid> loadPage() {
    return vidService.getAvailableVidPageHandler();
  }
}
