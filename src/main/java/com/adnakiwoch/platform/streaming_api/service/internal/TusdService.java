package com.adnakiwoch.platform.streaming_api.service.internal;

import com.adnakiwoch.platform.streaming_api.domain.Vid;
import com.adnakiwoch.platform.streaming_api.dto.request.hook.tusd.TusdHook;
import com.adnakiwoch.platform.streaming_api.dto.response.hooks.tusd.HTTPTusdResponse;
import com.adnakiwoch.platform.streaming_api.dto.response.hooks.tusd.HTTPTusdResponseBody;
import com.adnakiwoch.platform.streaming_api.dto.response.hooks.tusd.TusdResponse;
import com.adnakiwoch.platform.streaming_api.exception.resource.ResourceNotFoundException;
import com.adnakiwoch.platform.streaming_api.repository.VidRepo;
import com.adnakiwoch.platform.streaming_api.service.S3.S3Service;
import com.adnakiwoch.platform.streaming_api.service.security.FindUserDetails;
import com.adnakiwoch.platform.streaming_api.service.security.JwtService;
import enums.VidStat;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;
import tools.jackson.databind.ObjectMapper;

@Service
@Slf4j
public class TusdService {

  private final ObjectMapper objectMapper;
  private final JwtService jwtService;
  private final FindUserDetails findUserDetails;
  private final VidRepo vidRepo;
  private final S3Service s3Service;

  public TusdService(
      ObjectMapper objectMapper,
      JwtService jwtService,
      FindUserDetails findUserDetails,
      VidRepo vidRepo,
      S3Service s3Service) {
    this.objectMapper = objectMapper;
    this.jwtService = jwtService;
    this.findUserDetails = findUserDetails;
    this.vidRepo = vidRepo;
    this.s3Service = s3Service;
  }

  public ResponseEntity<TusdResponse> uploadRequest(TusdHook tusdHook) {
    switch (tusdHook.Type()) {
      case "pre-create":
        {
          String auth =
              tusdHook.Event().HTTPRequest().Header().get("Authorization").getFirst().substring(7);

          UUID userId = UUID.fromString(jwtService.extractUsername(auth));
          UserDetails userDetails = findUserDetails.loadUserById(userId);

          if (!jwtService.isTokenValid(auth, userDetails)) {
            HTTPTusdResponseBody bodyRaw = new HTTPTusdResponseBody("Not Auth");
            String body = objectMapper.writeValueAsString(bodyRaw);
            return ResponseEntity.status(HttpStatus.OK)
                .body(new TusdResponse(new HTTPTusdResponse(403, body), true));
          }

          if (tusdHook.Event().Upload().Size() > DataSize.parse("10GB").toBytes()) {
            log.info(
                "upload limit exided for teh file {} ",
                tusdHook.Event().Upload().MetaData().filename());

            HTTPTusdResponseBody bodyRaw = new HTTPTusdResponseBody("your file is to big");
            String body = objectMapper.writeValueAsString(bodyRaw);

            return ResponseEntity.status(HttpStatus.OK)
                .body(
                    new TusdResponse(
                        new HTTPTusdResponse(HttpStatus.PAYLOAD_TOO_LARGE.value(), body), true));
          }

          // meta data check, insuring match between meta data uplaod ID and db upload ID

          UUID vid_id = UUID.fromString(tusdHook.Event().Upload().MetaData().vid_id());
          Optional<Vid> vidOptional = vidRepo.findById(vid_id);
          if (vidOptional.isEmpty() || vidOptional.get().getVidStat() != VidStat.UPLOADREQ) {

            log.info("Invalid vid Id for the id  {} ", vid_id);

            HTTPTusdResponseBody bodyRaw =
                new HTTPTusdResponseBody(
                    "vid id doesn't exit make sure you have reqeust upload slot before  uploading, or is a new upload and not an old one");
            String body = objectMapper.writeValueAsString(bodyRaw);

            return ResponseEntity.status(HttpStatus.OK)
                .body(
                    new TusdResponse(
                        new HTTPTusdResponse(HttpStatus.FORBIDDEN.value(), body), true));
          }

          if (!(vidOptional.get().getUser().getId().equals(userId))) {

            log.info("could't match user id {} with vid id {} ", userId, vid_id);

            HTTPTusdResponseBody bodyRaw =
                new HTTPTusdResponseBody("coud't match video with user profile");
            String body = objectMapper.writeValueAsString(bodyRaw);

            return ResponseEntity.status(HttpStatus.OK)
                .body(
                    new TusdResponse(
                        new HTTPTusdResponse(HttpStatus.FORBIDDEN.value(), body), true));
          }

          vidOptional.get().setVidStat(VidStat.APPROVED);
          vidOptional.get().setSize(tusdHook.Event().Upload().Size());

          vidRepo.save(vidOptional.get());
          log.info("upload will start for user with id {} with vid id {} ", userId, vid_id);

          HTTPTusdResponseBody bodyRaw = new HTTPTusdResponseBody("upload will start");
          String body = objectMapper.writeValueAsString(bodyRaw);

          return ResponseEntity.status(HttpStatus.OK)
              .body(new TusdResponse(new HTTPTusdResponse(HttpStatus.OK.value(), body), false));
        }

      case "post-finish":
        {
          String vidLocation = tusdHook.Event().Upload().ID();

          UUID vid_id = UUID.fromString(tusdHook.Event().Upload().MetaData().vid_id());
          Optional<Vid> vidOptional = vidRepo.findById(vid_id);
          log.info(" the vid id loaded is, {}", vid_id);
          Vid vid =
              vidOptional.orElseThrow(
                  () -> new ResourceNotFoundException("vid with id:" + vid_id.toString()));
          vid.setUploadLocation(vidLocation);
          vid.setVidStat(VidStat.TUS_UPLOAD_COMPLETE);
          vid.setPresent(true);
          vidRepo.save(vid);

          HTTPTusdResponseBody bodyRaw = new HTTPTusdResponseBody("upload created start");
          String body = objectMapper.writeValueAsString(bodyRaw);

          String filePath = "./" + System.getenv("TEMP_UPLOAD") + "/" + vidLocation;

          s3Service.uploadFile(vidLocation, "raw-upload", filePath);

          return ResponseEntity.status(HttpStatus.OK)
              .body(new TusdResponse(new HTTPTusdResponse(HttpStatus.OK.value(), body), false));
        }

        // handle def hooks

      default:
        return ResponseEntity.status(HttpStatus.OK).build();
    }
  }
}
