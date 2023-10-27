package main

import (
	"fmt"
	"log/slog"
	"net/http"
	"os"

	"github.com/gin-gonic/gin"
	common "github.com/shiroyk/cowork/common/src/main/golang"
	doc "github.com/shiroyk/cowork/doc/api/src/main/golang/doc/client"
	user "github.com/shiroyk/cowork/user/api/src/main/golang/user/client"
)

const (
	banner = `
   ____      _ _       _     
  / ___|___ | | | __ _| |__  
 | |   / _ \| | |/ _ | '_ \ 
 | |__| (_) | | | (_| | |_) |
  \____\___/|_|_|\__,_|_.__/
`
	address = "localhost:8083"
)

var (
	router = engine()
	hub    = newHub()
)

var (
	wsKey     = slog.String("source", "ws")
	grpcKey   = slog.String("source", "grpc")
	streamKey = slog.String("source", "eventSubcribe")
)

func engine() *gin.Engine {
	gin.SetMode(common.ProfileValue[string](gin.DebugMode, gin.ReleaseMode))
	eng := gin.New()
	eng.NoRoute(func(ctx *gin.Context) {
		ctx.JSON(http.StatusNotFound, common.NewErrorMessage(http.StatusText(http.StatusNotFound)))
	})
	eng.Use(gin.Recovery())
	slog.SetDefault(common.NewLogger())
	eng.ForwardedByClientIP = true
	return eng
}

func main() {
	sub := subscribe()
	defer func() {
		nc().Drain()
		sub.Unsubscribe()
		user.UserServiceClient().Close()
		doc.DocServiceClient().Close()
	}()

	router.Any("/ping", func(ctx *gin.Context) { ctx.Writer.WriteHeader(http.StatusNoContent) })
	value := common.ProfileValue(address, ":8080")
	fmt.Printf("%s\n", common.Blue(banner))
	fmt.Printf("=> collab started on %s\n", common.Green(value))
	if err := router.Run(value); err != nil {
		slog.Error(err.Error())
		os.Exit(1)
	}
}
