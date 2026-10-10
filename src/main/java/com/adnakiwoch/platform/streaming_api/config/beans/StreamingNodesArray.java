package com.adnakiwoch.platform.streaming_api.config.beans;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor

public class StreamingNodesArray {
  // gonna need a thread safe array
  // already moved to redis whih is by design single threaded so just SET oprations on IT is enouph

  private  final RedisTemplate<String, Objects> redisTemplateForAvailableNodes;

  private final List<UUID> multitreadedList = new CopyOnWriteArrayList<UUID>();

  public void addElementToList(UUID uuid) {

    multitreadedList.add(uuid);
  }

  public UUID getElementAtIndex(int index) {
    return multitreadedList.get(index);
  }

  public int getArraysSize() {
    return multitreadedList.size();
  }

  public void removeElement(UUID nodeId) {
    multitreadedList.remove(nodeId);
  }
}
