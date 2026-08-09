package com.adnakiwoch.platform.streaming_api.repository;

import com.adnakiwoch.platform.streaming_api.domain.StreamingNode;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StreamingNodeRepo extends JpaRepository<StreamingNode, UUID> {

  Optional<StreamingNode> getStreamingNodeByIpAddr(String ipAddr);

  Optional<StreamingNode> getStreamingNodeById(UUID nodeId);

  List<StreamingNode> getStreamingNodeByUpStat(boolean stat);
}
