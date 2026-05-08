package com.adnakiwoch.platform.streaming_api.web.controller.hooks.encoder;

import com.adnakiwoch.platform.streaming_api.dto.request.hook.encode.EncodeDoneRequest;
import com.adnakiwoch.platform.streaming_api.dto.request.hook.encode.EncodeFailedRequest;
import com.adnakiwoch.platform.streaming_api.dto.response.hooks.encode.EncodeResponse;
import com.adnakiwoch.platform.streaming_api.service.internal.EncoderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/hooks/encode")
public class EncodeController {
  private final EncoderService encoderService;

  public EncodeController(EncoderService encoderService) {
    this.encoderService = encoderService;
  }

  @GetMapping("/fetch/new")
  public ResponseEntity<EncodeResponse> getVidToEncode() {
    return encoderService.getVidToEncode();
  }

  @PostMapping("/issue")
  public ResponseEntity<HttpStatus> handelEncodeProblem(
      @RequestBody EncodeFailedRequest encodeFailedRequest) {
    return encoderService.handleVidEncodeIssue(encodeFailedRequest);
  }

  @PostMapping("/done")
  public ResponseEntity<HttpStatus> handelEncodeDone(
      @RequestBody EncodeDoneRequest encodeDoneRequest) {
    return encoderService.encodeDone(encodeDoneRequest);
  }
}
