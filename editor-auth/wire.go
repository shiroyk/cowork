//go:build wireinject
// +build wireinject

package main

import (
	"github.com/google/wire"
)

func initialize() (runner, func(), error) {
	wire.Build(newConfig, newUserClient, newRedisClient, newTokenService, newRouter, newEngine, newRunner)
	return nil, nil, nil
}
