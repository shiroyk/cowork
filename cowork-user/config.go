package main

import (
	"fmt"
	"log/slog"
	"net"
	"net/http"
	"os"

	"github.com/gin-gonic/gin"
	common "github.com/shiroyk/cowork/common/golang"
	"github.com/shiroyk/cowork/user/api/generated/golang/api"
	"github.com/shiroyk/cowork/user/model"
	"github.com/shiroyk/cowork/user/router"
	"google.golang.org/grpc"
	"gorm.io/driver/mysql"
	"gorm.io/gorm"
)

const banner = `
  _   _
 | | | |___  ___ _ __
 | | | / __|/ _ \ '__|
 | |_| \__ \  __/ |
  \___/|___/\___|_|
`

type config struct {
	HTTP     string     `yaml:"http"`
	GRPC     string     `yaml:"grpc"`
	LogLevel slog.Level `yaml:"log-level"`
	MySQL    string     `yaml:"mysql"`
}

func newConfig() config {
	fmt.Printf("%s\n", common.Blue(banner))
	return common.Config(config{
		"localhost:8081",
		"localhost:9091",
		slog.LevelDebug,
		"dev:123456@tcp(localhost:3306)/dev?charset=utf8mb4&parseTime=True&loc=Local",
	})
}

func newDB(cfg config) (*gorm.DB, error) {
	db, err := gorm.Open(mysql.Open(cfg.MySQL))
	if err != nil {
		return nil, fmt.Errorf("failed to connect database: %w", err)
	}
	err = db.AutoMigrate(&model.UserInfo{}, &model.Session{})
	if err != nil {
		return nil, fmt.Errorf("failed to auto migrate: %w", err)
	}
	return db, nil
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
	eng.ForwardedByClientIP = true
	eng.Any("/ping", func(ctx *gin.Context) { ctx.Status(http.StatusNoContent) })
	return eng
}

func grpcServer(cfg config, s api.UserServiceServer) {
	defer func() {
		if r := recover(); r != nil {
			slog.Error(fmt.Sprintf("%s", r))
		}
	}()

	address := cfg.GRPC
	if address == "" {
		address = ":9090"
	}

	lis, err := net.Listen("tcp", address)
	if err != nil {
		panic(fmt.Errorf("grpc server failed to listen: %w", err))
	}
	var opts []grpc.ServerOption
	server := grpc.NewServer(opts...)
	api.RegisterUserServiceServer(server, s)
	fmt.Printf("=> user grpc started on %s\n", common.Green(address))
	if err = server.Serve(lis); err != nil {
		panic(fmt.Errorf("grpc server failed to serve: %w", err))
	}
}

type runner func()

func newRunner(
	cfg config,
	engine *gin.Engine,
	r *router.Router,
	server api.UserServiceServer,
) runner {
	r.Enable(engine)
	return func() {
		address := cfg.HTTP
		if address == "" {
			address = ":8080"
		}
		fmt.Printf("=> user http started on %s\n", common.Green(address))
		go grpcServer(cfg, server)
		if err := engine.Run(address); err != nil {
			slog.Error(err.Error())
			os.Exit(1)
		}
	}
}
