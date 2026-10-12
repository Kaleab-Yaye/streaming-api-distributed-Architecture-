package service

import (
	"log"
	"net/http"
	"strings"

	"github.com/jellydator/ttlcache/v3"
	"github.com/redis/go-redis/v9"
)

type VidAuthService struct {
	redisService              *RedisService
	currentFrameCache         *ttlcache.Cache[string, float64]
	accessTokenToWatchIdCache *ttlcache.Cache[string, string]
}

func NewVidAuthService(service *RedisService, currentFrameCache *ttlcache.Cache[string, float64], accessTokenToWatchIdCache *ttlcache.Cache[string, string]) *VidAuthService {

	return &VidAuthService{
		redisService:              service,
		currentFrameCache:         currentFrameCache,
		accessTokenToWatchIdCache: accessTokenToWatchIdCache,
	}
}

// does it exist logic here

func (vidAuthService *VidAuthService) WatchAuthHandler(rawUri string, accessToken string, frameBeingRequest float64) int {
	SplittedUri := strings.Split(rawUri, "/")
	// {}/vid/get/{vidLoc}/{quality}/{segmentNumber}
	// 0   1    2     3           4        5
	vidLoc := SplittedUri[3]
	key := vidLoc + ":" + accessToken

	// now we check for its exitance

	storedWatchId, err := vidAuthService.redisService.rdb.Get(vidAuthService.redisService.context, key).Result()

	if err == nil {
		log.Default().Println("there was an issue wiht accesing the value")
		log.Printf("the error is %s", err)
		return http.StatusInternalServerError
	}

	if err == redis.Nil {
		// remove this log for latter
		log.Printf("a cache was not found for the key %s", key)
		return http.StatusForbidden

	}

	if strings.HasSuffix(rawUri, ".ts") {

		vidAuthService.currentFrameCache.Set(storedWatchId, frameBeingRequest, ttlcache.DefaultTTL)

	}

	return http.StatusOK

}

func (VidAuthService *VidAuthService) doYouHaveWatchInCacheHandler(watchId string) float64 {

}

// the caching logic here ( which is going to be an asycn call)

// the do you have a catch with this
