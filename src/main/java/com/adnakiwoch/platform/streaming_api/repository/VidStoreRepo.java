package com.adnakiwoch.platform.streaming_api.repository;

import com.adnakiwoch.platform.streaming_api.domain.VidStoreLocation;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus
public interface VidStoreRepo extends JpaRepository<VidStoreLocation, UUID> {

  Optional<VidStoreLocation> getVidStoreLocationByVidId(UUID vidId);

  void deleteVidStoreLocationByStreamingNodeId(UUID vidId);
}
