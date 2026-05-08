package com.adnakiwoch.platform.streaming_api.service.vid;

import com.adnakiwoch.platform.streaming_api.domain.Vid;
import com.adnakiwoch.platform.streaming_api.exception.resource.ResourceNotFoundException;
import com.adnakiwoch.platform.streaming_api.repository.VidRepo;
import com.adnakiwoch.platform.streaming_api.service.security.JwtService;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class VidServiceUtil {
  private final VidRepo vidRepo;

  public VidServiceUtil(JwtService jwtService, VidRepo vidRepo, WatchService watchService) {
    this.vidRepo = vidRepo;
  }

  @Cacheable(value = "vid_id")
  public UUID getCatchableVidIDFromVidLocation(String vidLoc) {
    Optional<Vid> vidIdOptional = vidRepo.getVidByEncodedLocation(vidLoc);

    Vid vid =
        vidIdOptional.orElseThrow(
            () ->
                new ResourceNotFoundException("the vid with that location does't exist:" + vidLoc));
    log.info(
        "if this is printed more than once cahce is not working :: ::::::::::::::::::::::::::::::::");
    return vidIdOptional.get().getId();
  }
}
