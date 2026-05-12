package com.adnakiwoch.platform.streaming_api.web.controller.hooks.encoder;

import com.adnakiwoch.platform.streaming_api.dto.request.hook.encode.EncodeDoneRequest;
import com.adnakiwoch.platform.streaming_api.dto.request.hook.encode.EncodeFailedRequest;
import com.adnakiwoch.platform.streaming_api.dto.response.hooks.encode.EncodeResponse;
import com.adnakiwoch.platform.streaming_api.service.internal.EncoderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/hooks/encode")
@Slf4j
public class EncodeController {
  private final EncoderService encoderService;

  public EncodeController(EncoderService encoderService) {
    this.encoderService = encoderService;
  }

  @GetMapping("/fetch/new")
  public ResponseEntity<EncodeResponse> getVidToEncode(HttpServletRequest request) {

    log.info(
        "reqeusted for a new vid with the mechin with the mechine nummber {}",
        request.getHeader("Mech-Number"));

    return encoderService.getVidToEncode(request.getHeader("Mech-Number"));
  }

  @PostMapping("/issue")
  public ResponseEntity<HttpStatus> handelEncodeProblem(
      HttpServletRequest request, @RequestBody EncodeFailedRequest encodeFailedRequest) {
    return encoderService.handleVidEncodeIssue(
        encodeFailedRequest, request.getHeader("Mech-Number"));
  }

  @PostMapping("/done")
  public ResponseEntity<HttpStatus> handelEncodeDone(
      @RequestBody EncodeDoneRequest encodeDoneRequest, HttpServletRequest request) {

    return encoderService.encodeDone(encodeDoneRequest, request.getHeader("Mech-Number"));
  }
}
