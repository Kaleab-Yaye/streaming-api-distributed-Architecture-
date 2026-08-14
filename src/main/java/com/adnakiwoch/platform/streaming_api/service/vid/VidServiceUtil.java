package com.adnakiwoch.platform.streaming_api.service.vid;

import com.adnakiwoch.platform.streaming_api.domain.Vid;
import com.adnakiwoch.platform.streaming_api.domain.VidStoreLocation;
import com.adnakiwoch.platform.streaming_api.exception.resource.ResourceNotFoundException;
import com.adnakiwoch.platform.streaming_api.repository.VidRepo;
import com.adnakiwoch.platform.streaming_api.service.internal.GoStreamingNodeService;
import com.adnakiwoch.platform.streaming_api.service.security.JwtService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import enums.VidStat;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class VidServiceUtil {
  private final VidRepo vidRepo;
  private final GoStreamingNodeService goStreamingNodeService;
  private final  VidStoreService vidStoreService;

  public VidServiceUtil(JwtService jwtService, VidRepo vidRepo, WatchService watchService, GoStreamingNodeService goStreamingNodeService, VidStoreService vidStoreService) {
    this.vidRepo = vidRepo;
    this.goStreamingNodeService = goStreamingNodeService;
    this.vidStoreService = vidStoreService;
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

    @Async
    public void handleNodeFailerCleanState(UUID nodId, UUID vidId) {

        // this a lock aquiring methode, it stops any other methode from having lock on this one
        if (!goStreamingNodeService.removeNode(nodId)) {
            return; // doen't the clean up to happen
        }

        List<VidStoreLocation> vidStoreLocations =
                goStreamingNodeService.retrieveAllVidAssociatedWithNode(nodId);

        for (VidStoreLocation vidStoreLocation : vidStoreLocations) {
            Vid vid = vidStoreLocation.getVid();
            if (vid.getId() != vidId) {
                vid.setVidStat(VidStat.ENCODED);
            }
        }

        vidStoreService.removeALlEntriesOFNode(nodId);
    }

    @Async
    public void handleNodeFailerCleanState(UUID nodId) {

        if (!goStreamingNodeService.removeNode(nodId)) {
            return;
        }

        List<VidStoreLocation> vidStoreLocations =
                goStreamingNodeService.retrieveAllVidAssociatedWithNode(nodId);

        for (VidStoreLocation vidStoreLocation : vidStoreLocations) {
            Vid vid = vidStoreLocation.getVid();
            vid.setVidStat(VidStat.ENCODED);
        }

        vidStoreService.removeALlEntriesOFNode(nodId);
    }


}
