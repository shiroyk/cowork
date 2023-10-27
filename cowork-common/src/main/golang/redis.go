package common

import (
	"os"
	"sync"

	"github.com/redis/go-redis/v9"
)

const (
	PrefixBlackListToken = "BLACKLIST_TOKEN_"
)

// RedisClient returns the redis client instance
var RedisClient = sync.OnceValue(func() redis.UniversalClient {
	if IsDev() {
		opt, err := redis.ParseURL("redis://default:dev@localhost:6379/0")
		if err != nil {
			panic(err)
		}
		return redis.NewClient(opt)
	}

	opt, err := redis.ParseClusterURL(os.Getenv("REDIS_URL"))
	if err != nil {
		panic(err)
	}
	return redis.NewClusterClient(opt)
})
