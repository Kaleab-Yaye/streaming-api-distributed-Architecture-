package service

import (
	"log"
	"net/http"
	"strings"

	"github.com/redis/go-redis/v9"
)

type VidAuthService struct {
	redisService *RedisService
}

func NewVidAuthService(service *RedisService) *VidAuthService {

	return &VidAuthService{
		redisService: service,
	}
}

// does it exist logic here

func (vidAuthService *VidAuthService) WatchAuthHandler(rawUri string, accessToken string) int {
	splitedUri := strings.Split(rawUri, "/")
	// {}/vid/get/{vidLoc}/{quality}/{segmentNumber}
	// 0   1    2     3           4        5
	vidLoc := splitedUri[3]
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

	// call the caching logic here
	print("the sotred watch id is ", storedWatchId)

	return http.StatusOK

}

// the caching logic here ( which is going to be an asycn call)

// the do you have a catch with this
