package com.adnakiwoch.platform.streaming_api.service.vid;

import com.adnakiwoch.platform.streaming_api.domain.User;
import com.adnakiwoch.platform.streaming_api.domain.Vid;
import com.adnakiwoch.platform.streaming_api.domain.Watch;
import com.adnakiwoch.platform.streaming_api.dto.header.GetVidHeader;
import com.adnakiwoch.platform.streaming_api.repository.UserRepository;
import com.adnakiwoch.platform.streaming_api.repository.VidRepo;
import com.adnakiwoch.platform.streaming_api.repository.WatchRepo;
import com.github.benmanes.caffeine.cache.Cache;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class WatchService {
  private final WatchRepo watchRepo;
  private final VidRepo vidRepo;
  private final UserRepository userRepository;
  private final WatchServiceUtil watchUtil;
  private final Cache<UUID, Double> cacheVidId;

  public WatchService(
      WatchRepo watchRepo,
      VidRepo vidRepo,
      UserRepository userRepository,
      WatchServiceUtil watchUtil,
      Cache<UUID, Double> cacheVidId) {
    this.watchRepo = watchRepo;
    this.vidRepo = vidRepo;
    this.userRepository = userRepository;
    this.watchUtil = watchUtil;
    this.cacheVidId = cacheVidId;
  }

  public Double watchVidRequestHandler(UUID vidId, UUID userID) {

    Optional<Watch> watchOptional = watchRepo.findByVidIdAndUserId(vidId, userID);
    if (watchOptional.isEmpty()) {
      Vid vid = vidRepo.getReferenceById(vidId);
      User user = userRepository.getReferenceById(userID);
      Watch watch = new Watch();
      watch.setVid(vid);
      watch.setUser(user);
      watch.setLiked(false);
      watch.setTimesRewatch(0);
      watch.setCurrentFrame(0.0);

      watchRepo.save(watch);

      log.info("saved the vid reqeust with the new ID:{}", watch.getId());

      return (0.0);
    }

    if (cacheVidId.asMap().containsKey(vidId)) {
      log.info("returning the latest sement {} from the catche", cacheVidId.getIfPresent(vidId));
      return cacheVidId.getIfPresent(vidId);
    }

    log.info("gave non new watch id Id with {}", watchOptional.get().getId());
    log.info("the letest current return is {}", watchOptional.get().getCurrentFrame());

    return (watchOptional.get().getCurrentFrame());
  }

  public ResponseEntity<HttpStatus> watchVidAuthHandler(
      GetVidHeader getVidHeader, UserDetails userDetails) {
    UUID userId = UUID.fromString(userDetails.getUsername());
    UUID vidId = getVidHeader.vidId();
    Double currentFrame = getVidHeader.currentFrame();
    log.info(
        "catchHandler is being called with user id {} vidId {} and and at frame {}",
        userId,
        vidId,
        currentFrame);
    cacheHandler(vidId, currentFrame);
    return ResponseEntity.status(HttpStatus.OK).build();
  }

  @Async
  public void cacheHandler(UUID vidID, Double currentNumber) {
    /* idk why i left this here, seems nonsesence
    UUID watchId = watchUtil.getCacheableWatchId(new GetWatchIdForCache(vidID, userId));

     */

    cacheVidId.put(vidID, currentNumber);
    log.info("cache handler is called and has put the value");
  }
}
