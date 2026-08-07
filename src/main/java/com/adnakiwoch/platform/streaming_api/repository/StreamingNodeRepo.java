package com.adnakiwoch.platform.streaming_api.repository;


import com.adnakiwoch.platform.streaming_api.domain.StreamingNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StreamingNodeRepo extends JpaRepository<StreamingNode, UUID > {

    Optional<StreamingNode> getStreamingNodeByIpAddr(String ipAddr);

}
