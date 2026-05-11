package com.adnakiwoch.platform.streaming_api.service.internal;

import com.adnakiwoch.platform.streaming_api.domain.Vid;
import com.adnakiwoch.platform.streaming_api.dto.request.hook.encode.EncodeDoneRequest;
import com.adnakiwoch.platform.streaming_api.dto.request.hook.encode.EncodeFailedRequest;
import com.adnakiwoch.platform.streaming_api.dto.response.hooks.encode.EncodeResponse;
import com.adnakiwoch.platform.streaming_api.repository.VidRepo;
import enums.VidStat;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EncoderService {
  private final VidRepo vidRepo;

  public EncoderService(VidRepo vidRepo) {
    this.vidRepo = vidRepo;
  }

  public ResponseEntity<EncodeResponse> getVidToEncode() {

    Optional<Vid> vidOptional = vidRepo.getVidTOBeEncoded();
    if (vidOptional.isEmpty()) {
      return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
    Vid vid = vidOptional.get();

    // vid.setVidStat(VidStat.ENCODING);
    vidRepo.save(vid);
    return ResponseEntity.ok().body(new EncodeResponse(vid.getUploadLocation(), vid.getId()));
  }

  public ResponseEntity<HttpStatus> handleVidEncodeIssue(EncodeFailedRequest encodeFailedRequest) {
    Optional<Vid> vidOptional = vidRepo.findById(encodeFailedRequest.vidId());

    if (vidOptional.isEmpty()) {
      log.warn("encoder was woking on non exiting file, vid id {}", encodeFailedRequest.vidId());
      return ResponseEntity.status(HttpStatus.OK).build();
    }

    Vid vid = vidOptional.get();

    if (encodeFailedRequest.fileDeleted()) {
      vid.setPresent(false);
    }

    if (encodeFailedRequest.notVid()) {
      vid.setVidStat(VidStat.NOT_VID);

    } else if (encodeFailedRequest.brokenVid()) {
      vid.setVidStat(VidStat.BROKEN_VID);

    } else if (encodeFailedRequest.issueNotSpecified()) {
      vid.setVidStat(VidStat.ENCODERR);
    }

    vidRepo.save(vid);
    return ResponseEntity.status(HttpStatus.OK).build();
  }

  public ResponseEntity<HttpStatus> encodeDone(EncodeDoneRequest encodeDoneRequest) {
    log.info("encode done reqeust was made with the id {}", encodeDoneRequest.vidId());
    log.info(
        "enode done reqeus  was mad with final locaiton {}", encodeDoneRequest.finalLocation());
    Optional<Vid> vidOptional = vidRepo.findById(encodeDoneRequest.vidId());
    log.info("encode done reqeust was made with the id {}", encodeDoneRequest.vidId());
    log.info("encode done reqeust wase made with the vid length of {}", encodeDoneRequest.length());
    if (vidOptional.isEmpty()) {
      log.warn(
          "encoder was working on no exsiting vid file with id:{}, and storage locaiton at {}",
          encodeDoneRequest.vidId(),
          encodeDoneRequest.finalLocation());
      return ResponseEntity.ok().build();
    }

    Vid vid = vidOptional.get();
    vid.setVidStat(VidStat.ENCODED);
    vid.setLength(encodeDoneRequest.length());
    vid.setEncodedLocation(encodeDoneRequest.finalLocation());

    vid.setPresent(!encodeDoneRequest.rawDeleted());
    log.info("setting up the vid file after complete is done going fine");

    if (!encodeDoneRequest.rawDeleted()) {
      log.warn(
          "encoder could't delte the tempuplaod location, vidID {}. and raw storage location {} ",
          encodeDoneRequest.vidId(),
          encodeDoneRequest.finalLocation());
      vid.setPresent(true);
    }

    vidRepo.save(vid);

    log.info(":::: vid was saved :::::::");

    return ResponseEntity.status(HttpStatus.OK).build();
  }
}
