package main

import (
	"fmt"
	"log/slog"
	"net/http"
	"os"

	"github.com/gin-gonic/gin"
	common "github.com/shiroyk/cowork/common/golang"
	docclient "github.com/shiroyk/cowork/doc/api/golang/client"
	userclient "github.com/shiroyk/cowork/user/api/golang/client"
)

const banner = `
   ____      _ _       _     
  / ___|___ | | | __ _| |__  
 | |   / _ \| | |/ _ | '_ \ 
 | |__| (_) | | | (_| | |_) |
  \____\___/|_|_|\__,_|_.__/
`

var (
	keyWS     = slog.String("source", "ws")
	keyGRPC   = slog.String("source", "grpc")
	keyStream = slog.String("source", "event")
)

type config struct {
	userclient.UserConfig `yaml:",inline"`
	docclient.DocConfig   `yaml:",inline"`
	HTTP                  string     `yaml:"http"`
	Redis                 string     `yaml:"redis"`
	Nats                  string     `yaml:"nats"`
	LogLevel              slog.Level `yaml:"log-level"`
}

func newConfig() config {
	fmt.Printf("%s\n", common.Blue(banner))
	return common.Config(config{
		HTTP:     "localhost:8084",
		Redis:    common.DevRedis,
		LogLevel: slog.LevelDebug,
		Nats:     "nats://localhost:4222",
	})
}

func newUserClient(cfg config) (*userclient.Client, func()) {
	client := userclient.NewClient(cfg.UserConfig)
	return client, func() { client.Close() }
}

func newDocClient(cfg config) (*docclient.Client, func()) {
	client := docclient.NewClient(cfg.DocConfig)
	return client, func() { client.Close() }
}

func newEngine(cfg config) *gin.Engine {
	level := cfg.LogLevel
	slog.SetLogLoggerLevel(level)
	switch level {
	case slog.LevelInfo:
		gin.SetMode(gin.ReleaseMode)
	default:
		gin.SetMode(gin.DebugMode)
	}
	slog.SetDefault(common.NewLogger(level))

	eng := gin.New()
	eng.NoRoute(func(ctx *gin.Context) {
		ctx.JSON(http.StatusNotFound, common.NewErrorMessage(http.StatusText(http.StatusNotFound)))
	})
	eng.Use(gin.Recovery(), func(ctx *gin.Context) {
		ctx.Request = ctx.Request.WithContext(common.RequestMetadata(ctx.Request))
		ctx.Next()
	})
	eng.Any("/ping", func(ctx *gin.Context) { ctx.Status(http.StatusNoContent) })
	eng.ForwardedByClientIP = true
	return eng
}

type runner func()

func newRunner(
	cfg config,
	engine *gin.Engine,
	hub *Hub,
) runner {
	hub.router(engine)
	return func() {
		address := cfg.HTTP
		if address == "" {
			address = ":8080"
		}
		fmt.Printf("=> collab http started on %s\n", common.Green(address))
		if err := engine.Run(address); err != nil {
			slog.Error(err.Error())
			os.Exit(1)
		}
	}
}
