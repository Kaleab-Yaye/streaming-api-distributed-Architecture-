package com.adnakiwoch.platform.streaming_api.service.internal;


import com.adnakiwoch.platform.streaming_api.domain.Watch;
import com.adnakiwoch.platform.streaming_api.dto.request.hook.streaming_node.SubmitCurrentFrameRequest;
import com.adnakiwoch.platform.streaming_api.repository.WatchRepo;
import com.adnakiwoch.platform.streaming_api.service.vid.VidService;
import com.adnakiwoch.platform.streaming_api.service.vid.WatchService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.RedisSubscribedConnectionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.util.Optional;

@Service
@RequiredArgsConstructor

public class StreamingNodeService {
    private final WatchRepo watchRepo;

    public ResponseEntity<Void> handleSubmitCurrentFrameFromNodeServer(SubmitCurrentFrameRequest submitCurrentFrameRequest){
        Watch watch  = watchRepo.findByVidId(submitCurrentFrameRequest.watchId()).
                orElseThrow(()->new RedisSubscribedConnectionException("node server reqeust to update the current time for a watch that doesn't exist"));
        watch.setCurrentFrame(submitCurrentFrameRequest.currentFrame());

        return  ResponseEntity.status(HttpStatus.OK).build();
    }
}
