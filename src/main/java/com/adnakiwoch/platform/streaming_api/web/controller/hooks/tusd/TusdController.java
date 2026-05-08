package com.adnakiwoch.platform.streaming_api.web.controller.hooks.tusd;

import com.adnakiwoch.platform.streaming_api.dto.request.hook.tusd.TusdHook;
import com.adnakiwoch.platform.streaming_api.dto.response.hooks.tusd.TusdResponse;
import com.adnakiwoch.platform.streaming_api.service.internal.TusdService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/hooks/tusd/upload")
@Slf4j
public class TusdController {

  private final TusdService tusdService;

  public TusdController(TusdService tusdService) {
    this.tusdService = tusdService;
  }

  @PostMapping
  public ResponseEntity<TusdResponse> tusdHub(@RequestBody TusdHook tusdHook) {

    return tusdService.uploadRequest(tusdHook);
  }
}
