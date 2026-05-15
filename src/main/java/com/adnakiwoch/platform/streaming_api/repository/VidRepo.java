package com.adnakiwoch.platform.streaming_api.repository;

import com.adnakiwoch.platform.streaming_api.domain.Vid;
import enums.VidStat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface VidRepo extends JpaRepository<Vid, UUID> {

  @Query(
      value =
          "SELECT v FROM Vid v WHERE v.vidStat = 'UPLOADED' ORDER BY v.uploadedAt DESC LIMIT 1 ")
  Optional<Vid> getVidTOBeEncoded();

  Optional<UUID> getVidIdByEncodedLocation(String vidLoc);

  Optional<Vid> getVidByEncodedLocation(String vidLoc);

  List<Vid> getVidByVidStatOrVidStat(VidStat vidStat, VidStat vidStat2);

  @Query(value = "SELECT v from Vid v WHERE  v.vidStat = 'ENCODING' AND  v.encodeInt = :encodeInt")
  Optional<Vid> checkMachineWork(int encodeInt);
}
