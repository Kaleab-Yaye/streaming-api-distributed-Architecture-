package com.adnakiwoch.platform.streaming_api.cashes;

import com.adnakiwoch.platform.streaming_api.domain.Watch;
import com.adnakiwoch.platform.streaming_api.exception.resource.ResourceNotFoundException;
import com.adnakiwoch.platform.streaming_api.repository.WatchRepo;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.Scheduler;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class CashConfig {

  private final WatchRepo watchRepo;

  public CashConfig(WatchRepo watchRepo) {
    this.watchRepo = watchRepo;
  }

  @Bean
  Cache<UUID, Double> getCurrentVidLocationCache() {
    return Caffeine.newBuilder()
        .expireAfterWrite(Duration.ofSeconds(30))
        .scheduler(Scheduler.systemScheduler())
        .removalListener(this::removeListener)
        .build();
  }

  // this shi is writing to database even if the data is removed because of replacement gotta be
  // fixed.
  private void removeListener(UUID vidId, Double currentFrame, RemovalCause cause) {
    Optional<Watch> watchRepoOptional = watchRepo.findByVidId(vidId);
    int x = 0;
    if (cause == RemovalCause.EXPIRED) {
      Watch watch =
          watchRepoOptional.orElseThrow(
              () -> new ResourceNotFoundException("could find the watch with Id: " + vidId));
      watch.setCurrentFrame(currentFrame);
      watchRepo.save(watch);
      x = 1;
    }

    log.info(
        "removed a vid catch wiht the id{} with the cause {} and latest frame at {}, with remove code {}",
        vidId,
        cause,
        currentFrame,
        x);
  }

  @Bean
  Cache<Integer, UUID> getCatchForMachineEncodingTracking() {
    return Caffeine.newBuilder().build();
  }
}
