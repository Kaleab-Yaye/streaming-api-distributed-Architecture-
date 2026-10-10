package com.adnakiwoch.platform.streaming_api.cashes;

import com.adnakiwoch.platform.streaming_api.domain.Watch;
import com.adnakiwoch.platform.streaming_api.exception.resource.ResourceNotFoundException;
import com.adnakiwoch.platform.streaming_api.repository.WatchRepo;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.Scheduler;
import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@Slf4j
public class CashConfig {

//

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(60)) //
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

        // 2. Build the CacheManager
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                // 3. Pre-define your exact cache names from your YAML screenshot
                .initialCacheNames(Set.of(
                        "privilege",
                        "watch_id",
                        "vid_id",
                        "encode_id",
                        "node_id_to_port_addr",
                        "nod_id_to_addr",
                        "vid_id_to_node"
                ))
                .build();
    }





    @Bean
    public RedisTemplate<String, Object> redisTemplateForAvailableStreamingNodesList(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);


        template.setKeySerializer(new StringRedisSerializer());

        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());

        return template;
    }



// --- the caffine age
//    private final WatchRepo watchRepo;
//
//  public CashConfig(WatchRepo watchRepo) {
//    this.watchRepo = watchRepo;
//  }
//
//  @Bean
//  Cache<UUID, Double> getCurrentVidLocationCache() {
//    return Caffeine.newBuilder()
//        .expireAfterWrite(Duration.ofSeconds(30))
//        .scheduler(Scheduler.systemScheduler())
//        .removalListener(this::removeListener)
//        .build();
//  }
//
//  // this shi is writing to database even if the data is removed because of replacement gotta be
//  // fixed.
//  private void removeListener(UUID vidId, Double currentFrame, RemovalCause cause) {
//    Optional<Watch> watchRepoOptional = watchRepo.findByVidId(vidId);
//    int x = 0;
//    if (cause == RemovalCause.EXPIRED) {
//      Watch watch =
//          watchRepoOptional.orElseThrow(
//              () -> new ResourceNotFoundException("could find the watch with Id: " + vidId));
//      watch.setCurrentFrame(currentFrame);
//      watchRepo.save(watch);
//      x = 1;
//    }
//
//    log.info(
//        "removed a vid catch wiht the id{} with the cause {} and latest frame at {}, with remove code {}",
//        vidId,
//        cause,
//        currentFrame,
//        x);
//  }
//
//  @Bean
//  Cache<Integer, UUID> getCatchForMachineEncodingTracking() {
//    return Caffeine.newBuilder().build();
//  }


}
