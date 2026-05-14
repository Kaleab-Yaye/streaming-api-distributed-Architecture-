package com.adnakiwoch.platform.streaming_api.service.vid;

import com.adnakiwoch.platform.streaming_api.domain.Vid;
import com.adnakiwoch.platform.streaming_api.dto.header.GetVidHeader;
import com.adnakiwoch.platform.streaming_api.dto.request.vid.WatchVIdRequest;
import com.adnakiwoch.platform.streaming_api.dto.response.vid.GetAvailableVid;
import com.adnakiwoch.platform.streaming_api.dto.response.vid.VidDtoForGetAvailableVidResponse;
import com.adnakiwoch.platform.streaming_api.dto.response.vid.WatchVidResponse;
import com.adnakiwoch.platform.streaming_api.exception.resource.ResourceNotFoundException;
import com.adnakiwoch.platform.streaming_api.repository.VidRepo;
import com.adnakiwoch.platform.streaming_api.service.security.JwtService;
import enums.VidStat;
import jakarta.servlet.http.HttpServletRequest;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.TypeCollector;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

  public VidService(
      JwtService jwtService,
      VidRepo vidRepo,
      WatchService watchService,
      VidServiceUtil vidServiceUtil, RestClient restClient ) {
    this.jwtService = jwtService;
    this.vidRepo = vidRepo;
    this.watchService = watchService;
    this.vidServiceUtil = vidServiceUtil;
    this.restClient = restClient;
  }

  public ResponseEntity<HttpStatus> watchVidAuthOrchestrator(
      HttpServletRequest request, UserDetails userDetails) {

    String uri = request.getHeader("request_uri");
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

      UUID vidId = vidServiceUtil.getCatchableVidIDFromVidLocation(vidLocation);

      GetVidHeader getVidHeader = new GetVidHeader(vidId, currentFrame);

      return watchService.watchVidAuthHandler(getVidHeader, userDetails);
    }

    return ResponseEntity.ok().build();
  }

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

    if(vid.getVidStat()==(VidStat.READY)){
        java.lang.Double currentFrame = watchService.watchVidRequestHandler(vid.getId(), userId);

        return ResponseEntity.status(HttpStatus.OK)
                .body(new WatchVidResponse(vid.getEncodedLocation(), currentFrame));

    }

      ResponseEntity<String> response = restClient.get()
              .uri("http://stream_node:8000/health")
              .retrieve()
              .toEntity(String.class);
      int statusCode = response.getStatusCode().value();
      log.info("satus code of the requst made to streaming_node is {}", response.getStatusCode().value());

      if(response.getStatusCode()==HttpStatus.OK){

          java.lang.Double currentFrame = watchService.watchVidRequestHandler(vid.getId(), userId);
          vid.setVidStat(VidStat.READY);
          vidRepo.save(vid);

          return ResponseEntity.status(HttpStatus.OK)
                  .body(new WatchVidResponse(vid.getEncodedLocation(), currentFrame));
      }

      if (response.getStatusCode()==HttpStatus.NOT_FOUND){
          vid.setVidStat(VidStat.STREAMING_NODE_DOWNLOADING_FAILED);
          vidRepo.save(vid);
          return  ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
      }
      if (response.getStatusCode()==HttpStatus.FAILED_DEPENDENCY){
          vid.setVidStat(VidStat.STREAMING_NODE_ZIPPING_FAILED);
          vidRepo.save(vid);
          return  ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
      }


      return  ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();



  }

  public ResponseEntity<GetAvailableVid> getAvailableVidPageHandler() {
    List<Vid> vidList = vidRepo.getVidByVidStatOrVidStat(VidStat.ENCODED, VidStat.READY);
    ArrayList<VidDtoForGetAvailableVidResponse> vidDtoList = new ArrayList<>();
    for (Vid vid : vidList) {
      vidDtoList.add(new VidDtoForGetAvailableVidResponse(vid.getId(), vid.getName()));
    }

    return ResponseEntity.ok().body(new GetAvailableVid(vidDtoList));
  }
}
