package com.adnakiwoch.platform.streaming_api.web.controller.user.upload;

import com.adnakiwoch.platform.streaming_api.dto.request.upload.UploadVidRequest;
import com.adnakiwoch.platform.streaming_api.dto.response.user.UploadResponse;
import com.adnakiwoch.platform.streaming_api.service.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/upload/vid")
public class UploadController {
  private final UserService userService;

  public UploadController(UserService userService) {

    this.userService = userService;
  }

  @PostMapping
  @PreAuthorize("CAN_UPLOAD")
  ResponseEntity<UploadResponse> uploadVid(
      @AuthenticationPrincipal UserDetails userDetails,
      @RequestBody UploadVidRequest uploadVidRequest) {
    return ResponseEntity.status(HttpStatus.ACCEPTED)
        .body(userService.uploadServ(userDetails, uploadVidRequest));
  }
}
