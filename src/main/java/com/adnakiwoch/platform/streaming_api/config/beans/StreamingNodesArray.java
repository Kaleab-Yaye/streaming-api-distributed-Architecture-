package com.adnakiwoch.platform.streaming_api.config.beans;


import org.springframework.stereotype.Component;

import java.security.PrivateKey;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class StreamingNodesArray {
    // gonna need a thread safe array

    private final List<UUID> multitreadedList = new CopyOnWriteArrayList<UUID>();

    public void addElementToList(UUID uuid){
        multitreadedList.add(uuid);
    }

    public UUID getElementAtIndex(int index){
        return multitreadedList.get(index);
    }

    public int getArraysSize(){
        return  multitreadedList.size();
    }

    public void removeElement(UUID nodeId){
        multitreadedList.remove(nodeId);
    }


}
