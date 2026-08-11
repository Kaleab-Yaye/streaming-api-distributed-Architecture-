package com.adnakiwoch.platform.streaming_api.service.vid;

import com.adnakiwoch.platform.streaming_api.domain.StreamingNode;
import com.adnakiwoch.platform.streaming_api.domain.Vid;
import com.adnakiwoch.platform.streaming_api.domain.VidStoreLocation;
import com.adnakiwoch.platform.streaming_api.exception.resource.ResourceNotFoundException;
import com.adnakiwoch.platform.streaming_api.repository.VidStoreRepo;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
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

    log.info(
        "NEW: the nod_id_to_port number is passed, if you see more than you should the cach is broken");
    Optional<VidStoreLocation> optionalVidStoreLocation =
        vidStoreRepo.getVidStoreLocationByVidId(vidId);
    return optionalVidStoreLocation
        .orElseThrow(
            () ->
                new ResourceNotFoundException("could't find relation with the vid id of " + vidId))
        .getStreamingNode()
        .getId();
  }


  @Transactional
  public void removeALlEntriesOFNode(UUID nodId) {
      log.info("trying to remove all interies associated with the node that has id {}", nodId);
    vidStoreRepo.deleteVidStoreLocationByStreamingNodeId(nodId);
  }
}
