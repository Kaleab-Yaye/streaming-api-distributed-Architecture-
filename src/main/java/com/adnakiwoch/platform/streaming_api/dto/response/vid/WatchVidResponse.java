package com.adnakiwoch.platform.streaming_api.dto.response.vid;

import java.util.UUID;

public class WatchVidResponse{
    public String address;
    public String vidLocation;
    public Double currentFrame;
   public String accessToken;
    public WatchVidResponse(String address, String vidLocation, Double currentFrame){
        this.address = address;
        this.vidLocation = vidLocation;
        this.currentFrame = currentFrame;
    }

    public WatchVidResponse getWatchVidResponseWithAccessToken (String accessToken){
         this.accessToken = accessToken;
         return this;
    }

}
