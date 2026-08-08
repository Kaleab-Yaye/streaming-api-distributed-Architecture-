package com.adnakiwoch.platform.streaming_api.service.vid;

import com.adnakiwoch.platform.streaming_api.domain.StreamingNode;
import com.adnakiwoch.platform.streaming_api.domain.Vid;
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
  private  final GoStreamingNodeService goStreamingNodeService;
  private final VidStoreService vidStoreService;

  public VidService(
      JwtService jwtService,
      VidRepo vidRepo,
      WatchService watchService,
      VidServiceUtil vidServiceUtil,
      RestClient restClient, GoStreamingNodeService goStreamingNodeService,
      VidStoreService vidStoreService) {
    this.jwtService = jwtService;
    this.vidRepo = vidRepo;
    this.watchService = watchService;
    this.vidServiceUtil = vidServiceUtil;
    this.restClient = restClient;
    this.goStreamingNodeService = goStreamingNodeService;
    this.vidStoreService = vidStoreService;
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


    // if the vid is in a node, this needs to be updated to give the ip adress and port the node that is sotring the vid
      // will work on it after  handling the new vid reqeust path
    if (vid.getVidStat() == (VidStat.READY)) {
      java.lang.Double currentFrame = watchService.watchVidRequestHandler(vid.getId(), userId);

      return handelIfVidIsOnNode(vid, userId);
    }

    log.info("goign to make prepare request to the streaming node");

    // gonna put the logic for fetchiing random  mechine ID from the NODE SERVICE
   return handleWhenVidIsNotOnAnyNode(vid, userId);
  }

  public ResponseEntity<GetAvailableVid> getAvailableVidPageHandler() {
    List<Vid> vidList = vidRepo.getVidByVidStatOrVidStat(VidStat.ENCODED, VidStat.READY);
    ArrayList<VidDtoForGetAvailableVidResponse> vidDtoList = new ArrayList<>();
    for (Vid vid : vidList) {
      vidDtoList.add(new VidDtoForGetAvailableVidResponse(vid.getId(), vid.getName()));
    }

    return ResponseEntity.ok().body(new GetAvailableVid(vidDtoList));
  }

  private ResponseEntity<WatchVidResponse> handelIfVidIsOnNode(Vid vid, UUID userId){

      UUID nodeId = vidStoreService.getNodIdAssociatedWithVidId(vid.getId());
      String nodeEndPOint = goStreamingNodeService.getIpAndPortAddr(nodeId);
      String uri = nodeEndPOint+"/stream/v1/node/health";

      //making are you ok request now to it

      ResponseEntity<String> response =
              restClient
                      .post()
                      .uri(uri) //
                      .retrieve()
                      .toEntity(String.class);
      int statusCode =
              response
                      .getStatusCode()
                      .value();

      if(statusCode!=HttpStatus.OK.value()){
          //
          log.info(" the server responsible for the video with vid id {} is down fixing state", vid.getId());

          ResponseEntity<WatchVidResponse> webResponseTobeGiven =  handleNodeFailerPrepareRequestedFileForStream(vid,userId);
          // now the async clean up methode goes herr


      }


  }

  private ResponseEntity<WatchVidResponse>  handleNodeFailerPrepareRequestedFileForStream(Vid vid ,UUID userId){
      vid.setVidStat(VidStat.ENCODED);
      return  handleWhenVidIsNotOnAnyNode(vid, userId);
  }



  private void handleNodeFailerCleanState(UUID nodId){


  };



  private ResponseEntity<WatchVidResponse>   handleWhenVidIsNotOnAnyNode(Vid vid, UUID userId)


    {

        StreamingNode selectedNode = goStreamingNodeService.selectRandomNode();

        String getIPAndPorAddressOFChosenMech = goStreamingNodeService.getIpAndPortAddr(
                selectedNode.getId()

        );

        log.info("the machine that is gonnna handle it has addr of {}", getIPAndPorAddressOFChosenMech);

        String uri = getIPAndPorAddressOFChosenMech+"/stream/node/prepare";

        // shoudl we add a s

        ResponseEntity<String> response =
                restClient
                        .post()
                        .uri(uri) // NOTE: here it will become
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

            java.lang.Double currentFrame = watchService.watchVidRequestHandler(vid.getId(), userId);
            vid.setVidStat(VidStat.READY);

            vidRepo.save(vid);


            //async methode
            vidStoreService.addNewVidToNodeRelation(selectedNode, vid);



            return ResponseEntity.status(HttpStatus.OK)
                    .body(new WatchVidResponse(vid.getEncodedLocation(), currentFrame));
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
