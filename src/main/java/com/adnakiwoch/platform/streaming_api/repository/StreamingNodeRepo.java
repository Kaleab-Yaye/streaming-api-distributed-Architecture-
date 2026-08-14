package com.adnakiwoch.platform.streaming_api.repository;

import com.adnakiwoch.platform.streaming_api.domain.StreamingNode;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StreamingNodeRepo extends JpaRepository<StreamingNode, UUID> {

  Optional<StreamingNode> getStreamingNodeByIpAddr(String ipAddr);

  Optional<StreamingNode> getStreamingNodeByIpAddrAndPortNumberAndUpStat(
      String ipAddr, int portNum, boolean stat);

  Optional<StreamingNode> getStreamingNodeById(UUID nodeId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query(value = "SELECT n FROM StreamingNode n WHERE n.id = :nodeId")
  Optional<StreamingNode> getStreamingNodeByIdForLockedWrite(@Param("nodeId") UUID nodeId);

  List<StreamingNode> getStreamingNodeByUpStat(boolean stat);
}
