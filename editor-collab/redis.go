package main

import (
	"fmt"

	"github.com/redis/go-redis/v9"
	common "github.com/shiroyk/crdt-editor/common/golang"
)

func newRedisClient(cfg config) (redis.UniversalClient, func(), error) {
	if common.IsDev() {
		opt, err := redis.ParseURL(common.DevRedis)
		if err != nil {
			return nil, nil, fmt.Errorf("failed to parse redis url: %w", err)
		}
		client := redis.NewClient(opt)
		return client, func() { client.Close() }, nil
	}

	opt, err := redis.ParseClusterURL(cfg.Redis)
	if err != nil {
		return nil, nil, fmt.Errorf("failed to parse redis url: %w", err)
	}
	client := redis.NewClusterClient(opt)
	return client, func() { client.Close() }, nil
}
