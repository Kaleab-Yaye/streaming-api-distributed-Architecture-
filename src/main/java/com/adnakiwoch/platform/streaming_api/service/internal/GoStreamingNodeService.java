package com.adnakiwoch.platform.streaming_api.service.internal;


import com.adnakiwoch.platform.streaming_api.domain.StreamingNode;
import com.adnakiwoch.platform.streaming_api.dto.request.StreamingNode.StreamingNodeRegistRequest;
import com.adnakiwoch.platform.streaming_api.exception.resource.ResourceNotFoundException;
import com.adnakiwoch.platform.streaming_api.repository.StreamingNodeRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GoStreamingNodeService {
    @Autowired
    private StreamingNodeRepo streamingNodeRepo;

    //so gonna need an end point for node to registor here

    // this is the end point the GO nodes will interact with after booting up

    public ResponseEntity<HttpStatus> liveNodeHandler (StreamingNodeRegistRequest streamingNodeRegistRequest
    ){

        Optional<StreamingNode> optionalStreamingNode = streamingNodeRepo.getStreamingNodeByIpAddr(streamingNodeRegistRequest.ip_addr());

        if(optionalStreamingNode.isPresent()){

            return ResponseEntity.ok().build();

        }

        StreamingNode newStreamingNode = new StreamingNode();
        newStreamingNode.setIpAddr(streamingNodeRegistRequest.ip_addr());
        newStreamingNode.setPortNumber(streamingNodeRegistRequest.port_number())
        ;
        newStreamingNode.setUpStat(true);

        // lets handel if the server tries to register twice, or has crashed before and want's to regist again


        streamingNodeRepo.save(newStreamingNode);

        return ResponseEntity.ok().build();

    }

}
