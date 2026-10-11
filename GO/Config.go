package main

import (
	"GO/DTOs"
	"bytes"
	"context"
	"encoding/json"
	"fmt"
	"log"
	"net/http"
	"os"
	"time"

	"github.com/jellydator/ttlcache/v3"
)

func GetCacheForTrackingCurrentFrame(httpClient *http.Client) *ttlcache.Cache[string, float64] {

	cache := ttlcache.New[string, float64](
		ttlcache.WithTTL[string, float64](time.Second * 30))

	cache.OnEviction(func(ctx context.Context, reason ttlcache.EvictionReason, i *ttlcache.Item[string, float64]) {

		if reason == ttlcache.EvictionReasonExpired {
			go updateCurrentWatchFrame(i.Key(), httpClient, i.Value()) // and asycn call if you wil

		}

	})

	return cache

}

func updateCurrentWatchFrame(watchId string, httpClient *http.Client, currentFrame float64) {

	request_body := DTOs.SubmitCurrenFrameDTO{
		WatchId:      watchId, // must be in a way that it dynamically looks up the machins ip addr
		CurrentFrame: currentFrame,
	}

	end_point := "/submit/current/frame"

	mersheld_reqeust_body, err := json.Marshal(request_body)
	request, err := http.NewRequest("POST", os.Getenv("CENTRAL_SERVER")+end_point, bytes.NewReader(mersheld_reqeust_body))

	if err != nil {
		fmt.Println("there was an error in seting up a reqeust the cache eviction listner is not posting changes to central server")
	}

	res, err := httpClient.Do(request)

	if err != nil {

		log.Fatal("{CACHE_EVICTION_LISNTER:}there was an error making a reqeust")

	}

	defer res.Body.Close()

	if res.StatusCode != http.StatusOK {
		log.Fatal("{CACHE_EVICITION_LISTNER}central server is not accepting the submite updated current frame", res.StatusCode)

	}

}
