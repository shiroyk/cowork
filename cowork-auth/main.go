package main

import (
	"fmt"
	"log/slog"
	"net/http"
	"strings"

	"github.com/labstack/echo/v4"
	"github.com/labstack/echo/v4/middleware"
	"github.com/labstack/gommon/log"
	common "github.com/shiroyk/cowork/common/src/main/golang"
	"github.com/shiroyk/cowork/user/api/src/main/golang/user/client"
)

const (
	banner = `
     _         _   _     
    / \  _   _| |_| |__  
   / _ \| | | | __| '_ \ 
  / ___ \ |_| | |_| | | |
 /_/   \_\__,_|\__|_| |_|
`
	address = "localhost:8082"
)

var router = echo.New()

func setLogOutput() {
	if common.IsDev() {
		return
	}
	slog.SetDefault(common.NewLogger())
	router.Logger.SetOutput(common.LoggerWriter())
}

func main() {
	setLogOutput()
	defer client.UserServiceClient().Close()
	router.HideBanner = true
	router.HidePort = true
	router.HTTPErrorHandler = errorHandler
	router.Logger.SetLevel(log.DEBUG)
	router.Use(middleware.Recover())
	router.RouteNotFound("/*", func(c echo.Context) error {
		msg := fmt.Sprintf("Not found path %s", strings.TrimPrefix(c.Request().RequestURI, "/api"))
		return &common.ApiError{Code: http.StatusNotFound, Message: msg}
	})
	fmt.Printf("%s\n", common.Blue(banner))
	value := common.ProfileValue(address, ":8080")
	fmt.Printf("=> auth started on %s\n", common.Green(value))
	router.Logger.Fatal(router.Start(value))
}
