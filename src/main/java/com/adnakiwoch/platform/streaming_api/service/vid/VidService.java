package com.adnakiwoch.platform.streaming_api.service.vid;

import com.adnakiwoch.platform.streaming_api.domain.StreamingNode;
import com.adnakiwoch.platform.streaming_api.domain.Vid;
import com.adnakiwoch.platform.streaming_api.domain.VidStoreLocation;
import com.adnakiwoch.platform.streaming_api.dto.header.GetVidHeader;
import com.adnakiwoch.platform.streaming_api.dto.request.outgoing.stream.PrepareVidForStreamRequest;
import com.adnakiwoch.platform.streaming_api.dto.request.vid.WatchVIdRequest;
import com.adnakiwoch.platform.streaming_api.dto.response.vid.GetAvailableVid;
import com.adnakiwoch.platform.streaming_api.dto.response.vid.VidDtoForGetAvailableVidResponse;
import com.adnakiwoch.platform.streaming_api.dto.response.vid.WatchVidResponse;
import com.adnakiwoch.platform.streaming_api.exception.resource.ResourceNotFoundException;
import com.adnakiwoch.platform.streaming_api.repository.VidRepo;
import com.adnakiwoch.platform.streaming_api.service.internal.GoStreamingNodeService;
import com.adnakiwoch.platform.streaming_api.service.security.JwtService;
import enums.VidStat;
import jakarta.servlet.http.HttpServletRequest;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
public class VidService {

  private final JwtService jwtService;
  private final VidRepo vidRepo;
  private final WatchService watchService;
  private final VidServiceUtil vidServiceUtil;
  private final RestClient restClient;
  private final GoStreamingNodeService goStreamingNodeService;
  private final VidStoreService vidStoreService;
  private final CacheManager cacheManager;

  public VidService(
      JwtService jwtService,
      VidRepo vidRepo,
      WatchService watchService,
      VidServiceUtil vidServiceUtil,
      RestClient restClient,
      GoStreamingNodeService goStreamingNodeService,
      VidStoreService vidStoreService,
      CacheManager cacheManager) {
    this.jwtService = jwtService;
    this.vidRepo = vidRepo;
    this.watchService = watchService;
    this.vidServiceUtil = vidServiceUtil;
    this.restClient = restClient;
    this.goStreamingNodeService = goStreamingNodeService;
    this.vidStoreService = vidStoreService;
    this.cacheManager = cacheManager;
  }

  public ResponseEntity<HttpStatus> watchVidAuthOrchestrator(
      HttpServletRequest request, UserDetails userDetails) {

    String uri = request.getHeader("request-uri");
    java.lang.Double currentFrame = Double.parseDouble(request.getHeader("current-frame"));
    log.info("the put frame is {}", currentFrame);
    if (uri.endsWith(".ts")) {

      String[] uriParts = uri.split("/");

      // get/vid/get/{vidLoc}/{quality}/{segmentNumber}
      // 0    1   2     3         4         5

      String vidLocation = uriParts[3];
      String segmentNumber = uriParts[5];

      log.info("vid location is: {}", vidLocation);
      log.info("segmentNumber is: {}", segmentNumber);

      UUID vidId =
          vidServiceUtil.getCatchableVidIDFromVidLocation(
              vidLocation); // the clinet wont send the vid id but the vid locaiton that waas given
      // to the vidoeo when it was uploaded

      GetVidHeader getVidHeader = new GetVidHeader(vidId, currentFrame);

      return watchService.watchVidAuthHandler(getVidHeader, userDetails);
    }

    return ResponseEntity.ok()
        .build(); // this is neededx becouse when the video is being server the player is not only
    // going to ask about .ts files but is gonna need to the master play lists and
    // other stuff
  }

  // gonna update this entire thing

  public ResponseEntity<WatchVidResponse> watchVid(
      WatchVIdRequest watchVIdRequest, UserDetails userDetails) {
    UUID userId = UUID.fromString(userDetails.getUsername());
    Optional<Vid> vidOptional = vidRepo.findById(watchVIdRequest.vidId());

    if (vidOptional.isEmpty()) {
      log.info(
          "user with the id {}, tried to acces vid that doesn exist with the vid id of {}",
          userId,
          watchVIdRequest.vidId());
      throw new ResourceNotFoundException(
          "vid with the follwoing ID doesn exist: " + watchVIdRequest.vidId());
    }
    Vid vid = vidOptional.get();

    java.lang.Double currentFrame = watchService.watchVidRequestHandler(vid.getId(), userId);

    // if the vid is in a node, this needs to be updated to give the ip adress and port the node
    // that is sotring the vid
    // will work on it after  handling the new vid reqeust path
    if (vid.getVidStat() == (VidStat.READY)) {

      return handelIfVidIsOnNode(vid, userId, currentFrame);
    }

    log.info("goign to make prepare request to the streaming node");

    // gonna put the logic for fetchiing random  mechine ID from the NODE SERVICE
    return handleWhenVidIsNotOnAnyNode(vid, userId, currentFrame);
  }

  public ResponseEntity<GetAvailableVid> getAvailableVidPageHandler() {
    List<Vid> vidList = vidRepo.getVidByVidStatOrVidStat(VidStat.ENCODED, VidStat.READY);
    ArrayList<VidDtoForGetAvailableVidResponse> vidDtoList = new ArrayList<>();
    for (Vid vid : vidList) {
      vidDtoList.add(new VidDtoForGetAvailableVidResponse(vid.getId(), vid.getName()));
    }

    return ResponseEntity.ok().body(new GetAvailableVid(vidDtoList));
  }

  private ResponseEntity<WatchVidResponse> handelIfVidIsOnNode(
      Vid vid, UUID userId, double currentFrame) {

    UUID nodeId = vidStoreService.getNodIdAssociatedWithVidId(vid.getId());
    String nodeEndPoint = goStreamingNodeService.getIpAndPortAddr(nodeId); // this probably causing the non unique fetch but how?
    // so the thing this end point will work find for prod but forr local dev,i woudl ahve to create
    // another uri for dev part only wher
    //
    String uri = nodeEndPoint + "/stream/v1/node/health";
    String uriLocalDev =
        "http://host.docker.internal:"
            + goStreamingNodeService.getPortAddr(nodeId)
            + "/stream/v1/node/health";

    // making are you ok request now to it

    // so here is the thing the ClosedChannle Exception is thrown when there is no server behind to
    // talk to so, what should
    // happen is we handel the exception and make the request
    try {
      ResponseEntity<String> response =
          restClient
              .post()
              .uri(uriLocalDev) //
              .retrieve()
              .toEntity(String.class);

      int statusCode = response.getStatusCode().value();

      if (statusCode != HttpStatus.OK.value()) {
        //
        log.info(
            " the server responsible for the video with vid id {} is answering with non standard status code",
            vid.getId());

        ResponseEntity<WatchVidResponse> webResponseTobeGiven =
            handleNodeFailerPrepareRequestedFileForStream(vid, userId, nodeId, currentFrame);
        // now the async clean up methode goes herr

        handleNodeFailerCleanState(nodeId, vid.getId());

        return webResponseTobeGiven;
      }
      log.info("NEW: server with the file is live and returning propor values back.");
      return ResponseEntity.status(HttpStatus.OK)
          .body(new WatchVidResponse(nodeEndPoint, vid.getEncodedLocation(), currentFrame));

    } catch (Exception ex) {

      log.warn("an exception was thrown with the message {}", ex.getMessage());

      log.info(
          " the server responsible for the video with vid id {} is down fixing state", vid.getId());

      ResponseEntity<WatchVidResponse> webResponseTobeGiven =
          handleNodeFailerPrepareRequestedFileForStream(vid, userId, nodeId, currentFrame);
      // now the async clean up methode goes herr

      handleNodeFailerCleanState(nodeId, vid.getId());

      return webResponseTobeGiven;
    }
  }

  private ResponseEntity<WatchVidResponse> handleNodeFailerPrepareRequestedFileForStream(
      Vid vid, UUID userId, UUID streamingNodeId, Double currentFrame) {
    vid.setVidStat(VidStat.ENCODED);
    goStreamingNodeService.removeIdFromList(vid.getId());
    // also lets invalidate the catch that we holds the vid to node mapping
    log.info("evicting the catch with the key {}", vid.getId());
    cacheManager.getCache("vid_id_to_node").evict(vid.getId());
    log.info("evicting the node id to adress catch with the key {}", streamingNodeId);
    cacheManager.getCache("nod_id_to_addr").evict(streamingNodeId);
    cacheManager.getCache("node_id_to_port_addr").evict(streamingNodeId);

    return handleWhenVidIsNotOnAnyNode(vid, userId, currentFrame);
  }

  @Async
  public void handleNodeFailerCleanState(UUID nodId, UUID vidId) {
    // gonna need to fetch all the videos associated witt the node
    // remove each relation that was in the data base
    // and then updated the state of the vid files one by one ( gonna loop over thema nd cahnge them
    // to the encoded state, except for the video that was just proccessed)
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
  ;

  private ResponseEntity<WatchVidResponse> handleWhenVidIsNotOnAnyNode(
      Vid vid, UUID userId, Double currentFrame) {

    if (goStreamingNodeService.returnSizeOfTheArray() == 0) {

      return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }

    StreamingNode selectedNode = goStreamingNodeService.selectRandomNode();

    String getIPAndPorAddressOFChosenMech =
        goStreamingNodeService.getIpAndPortAddr(selectedNode.getId());

    log.info("the machine that is gonnna handle it has addr of {}", getIPAndPorAddressOFChosenMech);

    String uri = getIPAndPorAddressOFChosenMech + "/stream/node/prepare";
    String uriLocalDev =
        "http://host.docker.internal:"
            + goStreamingNodeService.getPortAddr(selectedNode.getId())
            + "/stream/node/prepare";

    log.info("the request to be made as the uri of {}", uriLocalDev);

    ResponseEntity<String> response =
        restClient
            .post()
            .uri(uriLocalDev)
            // the Go end point, and we
            // will have to move it to
            // env var
            .contentType(MediaType.APPLICATION_JSON)
            .body(new PrepareVidForStreamRequest(vid.getEncodedLocation(), "encoded"))
            .retrieve()
            .toEntity(String.class);
    int statusCode =
        response
            .getStatusCode()
            .value(); // NOTE: Go end points need an update to give the propor response status code
    // for the state machine to work on
    log.info(
        "satus code of the requst made to streaming_node is {}", response.getStatusCode().value());

    // now the follwing part has to be broken  down and made to be an async one. ( lets go)

    if (response.getStatusCode() == HttpStatus.OK) {

      vid.setVidStat(VidStat.READY);

      vidRepo.save(vid);

      // async method
      vidStoreService.addNewVidToNodeRelation(selectedNode, vid);

      return ResponseEntity.status(HttpStatus.OK)
          .body(
              new WatchVidResponse(
                  getIPAndPorAddressOFChosenMech,
                  vid.getEncodedLocation(),
                  currentFrame)); // the response has to be fixed as well it should include the
      // addrs as well
    }

    if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
      vid.setVidStat(VidStat.STREAMING_NODE_DOWNLOADING_FAILED);
      vidRepo.save(vid);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
    if (response.getStatusCode() == HttpStatus.FAILED_DEPENDENCY) {
      vid.setVidStat(VidStat.STREAMING_NODE_ZIPPING_FAILED);
      vidRepo.save(vid);

      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
  }
}
