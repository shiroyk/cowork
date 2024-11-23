package main

import (
	"fmt"
	"log/slog"
	"net/http"
	"strings"

	"github.com/labstack/echo/v4"
	"github.com/labstack/echo/v4/middleware"
	"github.com/labstack/gommon/log"
	common "github.com/shiroyk/crdt-editor/common/golang"
	userclient "github.com/shiroyk/crdt-editor/user/api/golang/client"
)

const banner = `
     _         _   _     
    / \  _   _| |_| |__  
   / _ \| | | | __| '_ \ 
  / ___ \ |_| | |_| | | |
 /_/   \_\__,_|\__|_| |_|
`

type config struct {
	userclient.UserConfig `yaml:",inline"`
	HTTP                  string     `yaml:"http"`
	Redis                 string     `yaml:"redis"`
	LogLevel              slog.Level `yaml:"log-level"`
	JwtSecret             string     `yaml:"jwt-secret"`
}

func newConfig() config {
	fmt.Printf("%s\n", common.Blue(banner))
	return common.Config(config{
		HTTP:      "localhost:8082",
		Redis:     common.DevRedis,
		LogLevel:  slog.LevelDebug,
		JwtSecret: "EDITOR",
	})
}

func newUserClient(cfg config) (*userclient.Client, func()) {
	client := userclient.NewClient(cfg.UserConfig)
	return client, func() { client.Close() }
}

func newEngine(cfg config) *echo.Echo {
	level := cfg.LogLevel
	slog.SetLogLoggerLevel(level)
	e := echo.New()
	switch level {
	case slog.LevelError:
		e.Logger.SetLevel(log.ERROR)
	case slog.LevelWarn:
		e.Logger.SetLevel(log.WARN)
	case slog.LevelInfo:
		e.Logger.SetLevel(log.INFO)
	default:
		e.Logger.SetLevel(log.DEBUG)
	}
	if common.IsK8s() {
		slog.SetDefault(common.NewLogger(level))
		e.Logger.SetOutput(common.LoggerWriter())
	}
	e.HideBanner = true
	e.HidePort = true
	e.HTTPErrorHandler = errorHandler
	e.Logger.SetLevel(log.DEBUG)
	e.Use(middleware.Recover(), func(next echo.HandlerFunc) echo.HandlerFunc {
		return func(ctx echo.Context) error {
			ctx.SetRequest(ctx.Request().WithContext(common.RequestMetadata(ctx.Request())))
			return next(ctx)
		}
	})
	e.RouteNotFound("/*", func(c echo.Context) error {
		msg := fmt.Sprintf("Not found path %s", strings.TrimPrefix(c.Request().RequestURI, "/api"))
		return &common.ApiError{Code: http.StatusNotFound, Message: msg}
	})
	e.Any("/ping", func(c echo.Context) error { return c.NoContent(http.StatusOK) })
	return e
}

type runner func()

func newRunner(cfg config, e *echo.Echo, router *router) runner {
	router.enable(e)
	return func() {
		address := cfg.HTTP
		if address == "" {
			address = ":8080"
		}
		fmt.Printf("=> auth http started on %s\n", common.Green(address))
		e.Logger.Fatal(e.Start(address))
	}
}
