package service

import (
	"context"

	"github.com/redis/go-redis/v9"
)

type RedisService struct {
	rdb     *redis.Client
	context context.Context
}

func (redisService *RedisService) getValue(key string) (string, error) {
	return redisService.rdb.Get(redisService.context, key).Result()
}

func NewRedisService(context context.Context, redisClient *redis.Client) *RedisService {
	return &RedisService{
		rdb:     redisClient,
		context: context,
	}
}
