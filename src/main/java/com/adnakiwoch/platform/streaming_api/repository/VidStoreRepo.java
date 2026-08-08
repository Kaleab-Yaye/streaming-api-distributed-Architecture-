package com.adnakiwoch.platform.streaming_api.repository;


import com.adnakiwoch.platform.streaming_api.domain.Vid;
import com.adnakiwoch.platform.streaming_api.domain.VidStoreLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Optional;
import java.util.UUID;

@ResponseStatus
public interface VidStoreRepo extends JpaRepository<VidStoreLocation, UUID> {


    Optional<VidStoreLocation> getVidStoreLocationByVidId(UUID vidId);


}
