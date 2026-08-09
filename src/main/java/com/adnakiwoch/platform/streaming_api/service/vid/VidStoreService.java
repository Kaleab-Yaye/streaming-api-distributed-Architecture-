package com.adnakiwoch.platform.streaming_api.service.vid;

import com.adnakiwoch.platform.streaming_api.domain.StreamingNode;
import com.adnakiwoch.platform.streaming_api.domain.Vid;
import com.adnakiwoch.platform.streaming_api.domain.VidStoreLocation;
import com.adnakiwoch.platform.streaming_api.exception.resource.ResourceNotFoundException;
import com.adnakiwoch.platform.streaming_api.repository.VidStoreRepo;
import java.util.Optional;
import java.util.UUID;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class VidStoreService {
  private final VidStoreRepo vidStoreRepo;

  VidStoreService(VidStoreRepo vidStoreRepo) {
    this.vidStoreRepo = vidStoreRepo;
  }

  @Async
  public void addNewVidToNodeRelation(StreamingNode selectedNode, Vid reqeustedVid) {

    VidStoreLocation vidStoreLocation = new VidStoreLocation();
    vidStoreLocation.setVid(reqeustedVid);
    vidStoreLocation.setStreamingNode(selectedNode);

    vidStoreRepo.save(vidStoreLocation);
  }

  @Cacheable(value = "vid_id_to_node", key = "#vidId")
  public UUID getNodIdAssociatedWithVidId(UUID vidId) {
    Optional<VidStoreLocation> optionalVidStoreLocation =
        vidStoreRepo.getVidStoreLocationByVidId(vidId);
    return optionalVidStoreLocation
        .orElseThrow(
            () ->
                new ResourceNotFoundException("could't find relation with the vid id of " + vidId))
        .getStreamingNode()
        .getId();
  }

  public void removeALlEntriesOFNode(UUID nodId) {

    vidStoreRepo.deleteVidStoreLocationByStreamingNodeId(nodId);
  }
}
