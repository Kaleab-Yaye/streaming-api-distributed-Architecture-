package com.adnakiwoch.platform.streaming_api.repository;

import com.adnakiwoch.platform.streaming_api.domain.Watch;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WatchRepo extends JpaRepository<Watch, UUID> {
  Optional<Watch> findByVidIdAndUserId(UUID vidId, UUID userId);

  Optional<Watch> findByVidId(UUID vidId);
}
