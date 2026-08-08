package com.adnakiwoch.platform.streaming_api.web.controller.hooks.StreamingNode;

import com.adnakiwoch.platform.streaming_api.dto.request.StreamingNode.StreamingNodeRegistRequest;
import com.adnakiwoch.platform.streaming_api.service.internal.GoStreamingNodeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/encoder")
public class StreamingNodeWebController {

  private final GoStreamingNodeService goStreamingNodeService;

  public StreamingNodeWebController(GoStreamingNodeService goStreamingNodeService) {
    this.goStreamingNodeService = goStreamingNodeService;
  }

  @PostMapping(
      "/regist/new") //  "/api/v1/encoder/regist/new" should be allowed to pass with not auth check
  // for now
  ResponseEntity<HttpStatus> registNewStreamingNodeController(
      @RequestBody StreamingNodeRegistRequest streamingNodeRegistRequest) {

    return goStreamingNodeService.newStreamingNodeHandler(streamingNodeRegistRequest);
  }
}
