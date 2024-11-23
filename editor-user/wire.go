//go:build wireinject
// +build wireinject

package main

import (
	"github.com/google/wire"
	"github.com/shiroyk/crdt-editor/user/router"
	"github.com/shiroyk/crdt-editor/user/service"
)

var serviceSets = wire.NewSet(service.NewUserService, service.NewGrpcService)

func initialize() (runner, error) {
	wire.Build(newConfig, newDB, serviceSets, newEngine, router.NewRouter, newRunner)
	return nil, nil
}
