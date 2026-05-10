package com.adnakiwoch.platform.streaming_api.service.S3;

import com.adnakiwoch.platform.streaming_api.domain.Vid;
import com.adnakiwoch.platform.streaming_api.exception.resource.ResourceNotFoundException;
import com.adnakiwoch.platform.streaming_api.repository.VidRepo;
import enums.VidStat;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@Slf4j
public class S3Service {
  private final S3Client s3Client;
  private final VidRepo vidRepo;

  public S3Service(S3Client s3Client, VidRepo vidRepo) {
    this.s3Client = s3Client;
    this.vidRepo = vidRepo;
  }

  @Async
  public void uploadFile(String key, String bucket, String file_path) {
    s3Client.putObject(
        PutObjectRequest.builder().bucket(bucket).key(key).build(), Path.of(file_path));
    Optional<Vid> vidOptional = vidRepo.findById(UUID.fromString(key));
    log.info("file uploaded to s3");
    Vid vid =
        vidOptional.orElseThrow(
            () ->
                new ResourceNotFoundException(
                    "S3 service uploader can't find a vid with the id: " + key));
    vid.setVidStat(VidStat.UPLOADED_NOT_DELETED);
    try {
      if (Files.deleteIfExists(Path.of(file_path))) {
        vid.setVidStat(VidStat.UPLOADED);
        log.info("deleted the vid with id {} from local storage", key);
        vidRepo.save(vid);
      } else {
        log.info("the vid with id {} doesn't exist on the local storage", key);
      }
    } catch (IOException exception) {
      log.warn("exception while deleting vid with id {}", key, exception);
    }
  }
}
