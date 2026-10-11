package main

import (
	"GO/Internal/Controller"
	"GO/Internal/service"
	"context"

	"github.com/redis/go-redis/v9"
)

type app struct {
	redisController *Controller.VidAuthController
}

func newAPP() *app {
	// let me setup  RDP first
	rdp, contextBack := setUpRedisClient()
	// now setup the Redis service

	redisService := service.NewRedisService(contextBack, rdp)
	vidAuthService := service.NewVidAuthService(redisService)
	vidAuthController := Controller.NewVidAuthController(vidAuthService)
	return &app{
		redisController: vidAuthController,
	}

}

func setUpRedisClient() (*redis.Client, context.Context) {

	rdb := redis.NewClient(&redis.Options{
		Addr:     "localhost:6379",
		Password: "", // will be an env variable for docker compose
		DB:       0,
		Protocol: 2,
	})

	// test connection

	err := rdb.Set(context.Background(), "test", "passed", 0).Err()

	if err != nil {
		panic(err)
	}

	val, err := rdb.Get(context.Background(), "test").Result()
	if err != nil {
		panic(err)
	}

	if val != "passed" {
		panic(new("the values did't match lol"))
	}

	return rdb, context.Background()
}
