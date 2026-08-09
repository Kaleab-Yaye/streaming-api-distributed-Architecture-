package com.adnakiwoch.platform.streaming_api.service.internal;

import com.adnakiwoch.platform.streaming_api.config.beans.StreamingNodesArray;
import com.adnakiwoch.platform.streaming_api.domain.StreamingNode;
import com.adnakiwoch.platform.streaming_api.domain.VidStoreLocation;
import com.adnakiwoch.platform.streaming_api.dto.request.StreamingNode.StreamingNodeRegistRequest;
import com.adnakiwoch.platform.streaming_api.exception.resource.ResourceNotFoundException;
import com.adnakiwoch.platform.streaming_api.repository.StreamingNodeRepo;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class GoStreamingNodeService {
  @Autowired private StreamingNodeRepo streamingNodeRepo;
  @Autowired private StreamingNodesArray streamingNodesArray;

  // so gonna need an end point for node to registor here

  // this is the end point the GO nodes will interact with after booting up

  public ResponseEntity<HttpStatus> newStreamingNodeHandler(
      StreamingNodeRegistRequest streamingNodeRegistRequest) {

    Optional<StreamingNode> optionalStreamingNode =
        streamingNodeRepo.getStreamingNodeByIpAddr(streamingNodeRegistRequest.ip_addr());

    if (optionalStreamingNode.isPresent()) {
      if (optionalStreamingNode.get().isUpStat()) {

        return ResponseEntity.ok().build();
      }
    }

    StreamingNode newStreamingNode = new StreamingNode();
    newStreamingNode.setIpAddr(streamingNodeRegistRequest.ip_addr());
    newStreamingNode.setPortNumber(streamingNodeRegistRequest.port_number());
    newStreamingNode.setUpStat(true);

    // how should the catch here,  we can create this ipadress + port
    streamingNodeRepo.save(newStreamingNode);

    // gotta save it to the array now

    log.info("saving the nodes id {} to multithreaded array", newStreamingNode.getId());

    streamingNodesArray.addElementToList(newStreamingNode.getId());

    return ResponseEntity.ok().build();
  }

  public StreamingNode selectRandomNode() {
    int size = streamingNodesArray.getArraysSize();
    Random random = new Random();
    UUID random_uuid = streamingNodesArray.getElementAtIndex(random.nextInt(size));
    Optional<StreamingNode> optionalStreamingNode =
        streamingNodeRepo.getStreamingNodeById(random_uuid);

    return optionalStreamingNode.orElseThrow(
        () ->
            new ResourceNotFoundException(
                "coud't find the vid  with the specifed id " + random_uuid)); // returning
  }

  @Cacheable(value = "nod_id_to_addr", key = "#nodid")
  public String getIpAndPortAddr(UUID nodId) {

    Optional<StreamingNode> optionalRandStreamingNode = streamingNodeRepo.findById(nodId);

    StreamingNode selectedStreamingNode =
        optionalRandStreamingNode.orElseThrow(
            () ->
                new ResourceNotFoundException("" + "could't find the vid with the ID {}" + nodId));

    // return the build ip addr

    return selectedStreamingNode.getIpAddr() + ":" + selectedStreamingNode.getPortNumber();
  }

  public void removeIdFromList(UUID nodeId) {
    streamingNodesArray.removeElement(nodeId);
  }

  public void changeNodeStat(UUID nodeID) {
    Optional<StreamingNode> optionalStreamingNode = streamingNodeRepo.getStreamingNodeById(nodeID);
    optionalStreamingNode
        .orElseThrow(() -> new ResourceNotFoundException("there is no vid with the id" + nodeID))
        .setUpStat(false);
  }

  public List<VidStoreLocation> retrieveAllVidAssociatedWithNode(UUID nodeId) {

    Optional<StreamingNode> optionalStreamingNode = streamingNodeRepo.getStreamingNodeById(nodeId);

    return optionalStreamingNode
        .orElseThrow(
            () -> new ResourceNotFoundException("there was no node with the Id of " + nodeId))
        .getVidStoreLocations();
  }
}
