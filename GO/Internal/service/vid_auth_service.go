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

	var storedWatchId string

	element := vidAuthService.accessTokenToWatchIdCache.Get(key)

	// now we check for its exitance

	if element == nil {

		var err error
		storedWatchId, err = vidAuthService.redisService.rdb.Get(vidAuthService.redisService.context, key).Result()

		if err == redis.Nil {
			// remove this log for latter
			log.Printf("a cache was not found for the key %s", key)
			return http.StatusForbidden

		}

		if err != nil {
			log.Default().Println("there was an issue wiht accesing the value")
			log.Printf("the error is %s", err)
			return http.StatusInternalServerError
		}

		vidAuthService.accessTokenToWatchIdCache.Set(key, storedWatchId, ttlcache.DefaultTTL)

	} else {

		storedWatchId = element.Value()

	}

	if !strings.HasSuffix(rawUri, ".ts") {

		vidAuthService.currentFrameCache.Set(storedWatchId, frameBeingRequest, ttlcache.DefaultTTL)

	}

	return http.StatusOK

}

func (VidAuthService *VidAuthService) doYouHaveWatchInCacheHandler(watchId string) float64 {

	item := VidAuthService.currentFrameCache.Get(watchId)

	if item == nil {

		return -1 // it litrally means i don't have it

	}

	return item.Value()

}
