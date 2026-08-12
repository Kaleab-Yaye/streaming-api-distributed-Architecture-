package com.adnakiwoch.platform.streaming_api.service.internal;

import com.adnakiwoch.platform.streaming_api.config.beans.StreamingNodesArray;
import com.adnakiwoch.platform.streaming_api.domain.StreamingNode;
import com.adnakiwoch.platform.streaming_api.domain.VidStoreLocation;
import com.adnakiwoch.platform.streaming_api.dto.request.StreamingNode.StreamingNodeRegistRequest;
import com.adnakiwoch.platform.streaming_api.exception.resource.ResourceNotFoundException;
import com.adnakiwoch.platform.streaming_api.repository.StreamingNodeRepo;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class GoStreamingNodeService implements CommandLineRunner {
  @Autowired private StreamingNodeRepo streamingNodeRepo;
  @Autowired private StreamingNodesArray streamingNodesArray;

  // so gonna need an end point for node to registor here

  // this is the end point the GO nodes will interact with after booting up

  public ResponseEntity<HttpStatus> newStreamingNodeHandler(
      StreamingNodeRegistRequest streamingNodeRegistRequest) {

    Optional<StreamingNode> optionalStreamingNode =
        streamingNodeRepo.getStreamingNodeByIpAddrAndPortNumberAndUpStat(
            streamingNodeRegistRequest.ip_addr(), streamingNodeRegistRequest.port_number(), true);

    if (optionalStreamingNode.isPresent()) {

      return ResponseEntity.ok().build();
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
    // we might have not handled the wat if the list is empety thing
    // now at startup the carch should be prebuilt before other stuff[
    UUID random_uuid = streamingNodesArray.getElementAtIndex(random.nextInt(size));
    Optional<StreamingNode> optionalStreamingNode =
        streamingNodeRepo.getStreamingNodeById(random_uuid);

    return optionalStreamingNode.orElseThrow(
        () ->
            new ResourceNotFoundException(
                "coud't find the vid  with the specifed id " + random_uuid)); // returning
  }

  @Cacheable(value = "nod_id_to_addr", key = "#nodId")
  public String getIpAndPortAddr(UUID nodId) {

    log.info(
        "NEW: the nod_id_to_addr number is passed, if you see more than you should the cach is broken");

    Optional<StreamingNode> optionalRandStreamingNode = streamingNodeRepo.findById(nodId);

    StreamingNode selectedStreamingNode =
        optionalRandStreamingNode.orElseThrow(
            () ->
                new ResourceNotFoundException("" + "could't find the vid with the ID {}" + nodId));

    // return the build ip addr

    return selectedStreamingNode.getIpAddr() + ":" + selectedStreamingNode.getPortNumber();
  }

  @Cacheable(value = "node_id_to_port_addr", key = "#nodId")
  public String getPortAddr(UUID nodId) {

    log.info(
        "NEW: the nod_id_to_port number is passed, if you see more than you should the cach is broken");

    Optional<StreamingNode> optionalRandStreamingNode = streamingNodeRepo.findById(nodId);

    StreamingNode selectedStreamingNode =
        optionalRandStreamingNode.orElseThrow(
            () ->
                new ResourceNotFoundException("" + "could't find the vid with the ID {}" + nodId));

    // return the build ip addr

    return String.valueOf(selectedStreamingNode.getPortNumber());
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

  @Override
  public void run(String... args) throws Exception {
    List<StreamingNode> listStreamingNodes = streamingNodeRepo.getStreamingNodeByUpStat(true);
    if (!listStreamingNodes.isEmpty()) {

      for (StreamingNode streamingNode : listStreamingNodes) {

        streamingNodesArray.addElementToList(streamingNode.getId());
      }
    }
  }

  public int returnSizeOfTheArray() {
    return streamingNodesArray.getArraysSize();
  }

  // this methode for now will be used to change stat of a node but can also be used to remove the
  // recored
  public void removeNode(UUID uuid) {
    // should
    Optional<StreamingNode> optionalStreamingNode = streamingNodeRepo.getStreamingNodeById(uuid);

    optionalStreamingNode
        .orElseThrow(() -> new ResourceNotFoundException("there is node with the id" + uuid))
        .setUpStat(false);
    optionalStreamingNode.get().setUpdatedAt(OffsetDateTime.now());
  }
}
