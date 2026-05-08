package com.adnakiwoch.platform.streaming_api.service.vid;

import com.adnakiwoch.platform.streaming_api.domain.Watch;
import com.adnakiwoch.platform.streaming_api.dto.internal.GetWatchIdForCache;
import com.adnakiwoch.platform.streaming_api.exception.resource.ResourceNotFoundException;
import com.adnakiwoch.platform.streaming_api.repository.WatchRepo;
import java.util.Optional;
import java.util.UUID;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class WatchServiceUtil {

  private final WatchRepo watchRepo;

  public WatchServiceUtil(WatchRepo watchRepo) {
    this.watchRepo = watchRepo;
  }

  @Cacheable(value = "watch_id", key = "#getWatchIdForCache")
  public UUID getCacheableWatchId(GetWatchIdForCache getWatchIdForCache) {
    Optional<Watch> watchOptional =
        watchRepo.findByVidIdAndUserId(getWatchIdForCache.vidId(), getWatchIdForCache.userId());
    Watch watch =
        watchOptional.orElseThrow(
            () ->
                new ResourceNotFoundException(
                    "watch history for this vid doesn't exist" + getWatchIdForCache.vidId()));
    return watch.getId();
  }
}
