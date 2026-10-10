package com.adnakiwoch.platform.streaming_api.config.beans;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Set;
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

  private  final RedisTemplate<String, UUID> redisTemplateForAvailableNodes;

  private final String nodeListKeyName = System.getenv("NODE_LIST_KEY_NAME");

  private final List<UUID> multitreadedList = new CopyOnWriteArrayList<UUID>();

  public void addElementToList(UUID uuid) {

      redisTemplateForAvailableNodes.opsForSet().add(nodeListKeyName ,uuid);


  }

  public UUID getElementAtIndex(int index) {
   Set<UUID>  setOfALlAvailableNodes =  redisTemplateForAvailableNodes.opsForSet().members(nodeListKeyName);
      assert setOfALlAvailableNodes != null;
      return setOfALlAvailableNodes.toArray(new UUID[0])[index];
  }

  public int getArraysSize() {
    return Math.toIntExact(redisTemplateForAvailableNodes.opsForSet().size(nodeListKeyName));
  }

  public void removeElement(UUID nodeId) {
    redisTemplateForAvailableNodes.opsForSet().remove(nodeListKeyName, nodeId);
  }
}
