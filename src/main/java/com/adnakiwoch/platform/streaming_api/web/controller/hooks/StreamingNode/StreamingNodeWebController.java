package com.adnakiwoch.platform.streaming_api.web.controller.hooks.StreamingNode;

import com.adnakiwoch.platform.streaming_api.dto.request.StreamingNode.StreamingNodeRegistRequest;
import com.adnakiwoch.platform.streaming_api.dto.request.hook.streaming_node.SubmitCurrentFrameRequest;
import com.adnakiwoch.platform.streaming_api.service.internal.GoStreamingNodeService;
import com.adnakiwoch.platform.streaming_api.service.internal.StreamingNodeService;
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
  private final StreamingNodeService streamingNodeService;

  public StreamingNodeWebController(GoStreamingNodeService goStreamingNodeService, StreamingNodeService streamingNodeService) {
    this.goStreamingNodeService = goStreamingNodeService;
    this.streamingNodeService = streamingNodeService;
  }

  @PostMapping(
      "/regist/new") //  "/api/v1/encoder/regist/new" should be allowed to pass with not auth check
  // for now
  ResponseEntity<HttpStatus> registNewStreamingNodeController(
      @RequestBody StreamingNodeRegistRequest streamingNodeRegistRequest) {

    return goStreamingNodeService.newStreamingNodeHandler(streamingNodeRegistRequest);
  }


  @PostMapping("/submite/current/frame")

   ResponseEntity<Void>  submiteCurrentFrameHandler (@RequestBody SubmitCurrentFrameRequest submitCurrentFrameRequest){

      return  streamingNodeService.handleSubmitCurrentFrameFromNodeServer(submitCurrentFrameRequest);

  }


}
