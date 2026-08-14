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
import com.adnakiwoch.platform.streaming_api.repository.StreamingNodeRepo;
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
import org.springframework.transaction.annotation.Transactional;
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
  private final StreamingNodeRepo streamingNodeRepo;

  public VidService(
      JwtService jwtService,
      VidRepo vidRepo,
      WatchService watchService,
      VidServiceUtil vidServiceUtil,
      RestClient restClient,
      GoStreamingNodeService goStreamingNodeService,
      VidStoreService vidStoreService,
      CacheManager cacheManager,
      StreamingNodeRepo streamingNodeRepo) {
    this.jwtService = jwtService;
    this.vidRepo = vidRepo;
    this.watchService = watchService;
    this.vidServiceUtil = vidServiceUtil;
    this.restClient = restClient;
    this.goStreamingNodeService = goStreamingNodeService;
    this.vidStoreService = vidStoreService;
    this.cacheManager = cacheManager;
    this.streamingNodeRepo = streamingNodeRepo;
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
  @Transactional
  public ResponseEntity<WatchVidResponse> watchVid(
      WatchVIdRequest watchVIdRequest, UserDetails userDetails) {
    UUID userId = UUID.fromString(userDetails.getUsername());

    // ok they should read it in a normal way then if they find it is encoded they should check if
    // whiel they are inside they fin

    Optional<Vid> vidOptional =
        vidRepo.getVidLockedToRead(
            watchVIdRequest.vidId()); // this shoudl be trasctional methode tha  has to lock the db

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

    if (vid.getVidStat() == (VidStat.READY)) {

      return handelIfVidIsOnNode(vid, userDetails, currentFrame);
    }

    log.info("goign to make prepare request to the streaming node");

    // gonna put the logic for fetchiing random  mechine ID from the NODE SERVICE
    return handleWhenVidIsNotOnAnyNode(vid, userDetails, currentFrame);
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
      Vid vid, UserDetails userDetails, double currentFrame) {

    // now this is where whe can apply one of the read locks, becosue if the clean up is working on
    // it there is not need to advance from here
    // we then see the sat and if it falase what i can do is move it back to teh watch vid methode
    // again
    UUID nodeId = vidStoreService.getNodIdAssociatedWithVidId(vid.getId());
    StreamingNode streamingNode = goStreamingNodeService.getStreamingNodeWithReadLock(nodeId);
    if (!streamingNode.isUpStat()) {
      return watchVid(
          new WatchVIdRequest(vid.getId()),
          userDetails); // yes self invocation happen in the trasaction but it is already in a big
      // trasaction so.
    }

    String nodeEndPoint =
        goStreamingNodeService.getIpAndPortAddr(
            nodeId); // this probably causing the non unique fetch but how?
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
            fileOwnerNodeHealthCheckFailed(vid, nodeId, currentFrame, userDetails);
        // now the async clean up methode goes herr

        vidServiceUtil.handleNodeFailerCleanState(nodeId, vid.getId());

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
          fileOwnerNodeHealthCheckFailed(vid, nodeId, currentFrame, userDetails);
      // now the async clean up methode goes herr

      vidServiceUtil.handleNodeFailerCleanState(nodeId, vid.getId());

      return webResponseTobeGiven;
    }
  }

  // this need to get refactored the name is confusing asfk

  private ResponseEntity<WatchVidResponse> fileOwnerNodeHealthCheckFailed(
      Vid vid, UUID streamingNodeId, Double currentFrame, UserDetails userDetails) {
    vid.setVidStat(VidStat.ENCODED);
    goStreamingNodeService.removeIdFromList(
        streamingNodeId); // how do you fuck up this bad, why do you fuck up this bad, how is this
    // even possible, how do you remove a vidf rom a node list? why kal why is
    // you dumb
    // also lets invalidate the catch that we holds the vid to node mapping
    log.info("evicting the catch with the key {}", vid.getId());
    cacheManager.getCache("vid_id_to_node").evict(vid.getId());
    log.info("evicting the node id to adress catch with the key {}", streamingNodeId);
    cacheManager.getCache("nod_id_to_addr").evict(streamingNodeId);
    cacheManager.getCache("node_id_to_port_addr").evict(streamingNodeId);

    return handleWhenVidIsNotOnAnyNode(
        vid,
        userDetails,
        currentFrame); // have to clean up those methode they are tkaing more than needed njebr of
    // arguments
  }

  private ResponseEntity<WatchVidResponse> handelNodeFailerToPrepareFileForStream(
      Vid vid, Double currentFrame, StreamingNode streamingNode, UserDetails userDetails) {
    UUID streamingNodeId = streamingNode.getId();
    goStreamingNodeService.removeIdFromList(streamingNodeId);
    log.info("evicting the node id to adress catch with the key {}", streamingNodeId);
    cacheManager.getCache("nod_id_to_addr").evict(streamingNodeId);
    cacheManager.getCache("node_id_to_port_addr").evict(streamingNodeId);

    return handleWhenVidIsNotOnAnyNode(vid, userDetails, currentFrame); // remove vid.getId();
  }


  private ResponseEntity<WatchVidResponse> handleWhenVidIsNotOnAnyNode(
      Vid vid, UserDetails userDetails, Double currentFrame) {

    Optional<Vid> optionalVid = vidRepo.getVidLockedToWrite(vid.getId());
    Vid toBeWrittenOnVid =
        optionalVid.orElseThrow(
            () ->
                new ResourceNotFoundException(
                    "the vid with the id is not found "
                        + vid.getId())); // this line here stops the simoltinuist
    if (toBeWrittenOnVid.getVidStat() == VidStat.READY) {
      return handelIfVidIsOnNode(vid, userDetails, currentFrame);
    }

    if (goStreamingNodeService.returnSizeOfTheArray() == 0) {

      return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }

    StreamingNode selectedNode = goStreamingNodeService.selectRandomNode();

    String getIPAndPorAddressOFChosenMech =
        goStreamingNodeService.getIpAndPortAddr(selectedNode.getId());

    log.info("the machine that is gonnna handle it has addr of {}", getIPAndPorAddressOFChosenMech);

    // gonna add this safe case when the prepare reqeust fails, and the same clean up happens!!

    try {
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
              .value(); // NOTE: Go end points need an update to give the propor response status
      // code

      // for the state machine to work on
      log.info(
          "satus code of the requst made to streaming_node is {}",
          response.getStatusCode().value());

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

    } catch (Exception ex) {
      // if exception is hrwon it means the end node is broken, so we gonna need need some recursive
      // calls to make another node handels it
      log.warn(
          "a network io exception was thrown, trying to order another node to prepare the file for stream {}",
          ex.getMessage());

      ResponseEntity<WatchVidResponse> watchVidResponseResponseEntity =
          handelNodeFailerToPrepareFileForStream(vid, currentFrame, selectedNode, userDetails);

      vidServiceUtil.handleNodeFailerCleanState(selectedNode.getId());

      return watchVidResponseResponseEntity;
    }
    // so the entire thing will go in recusrsive calles trying to find the correc  end point to
    // stream with.
  }
}
