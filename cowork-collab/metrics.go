package main

import (
	"net/http"

	"github.com/gin-gonic/gin"
)

func init() {
	router.Any("/metrics", metrics)
}

func metrics(ctx *gin.Context) {
	ctx.JSON(http.StatusOK, map[string]any{"users": hub.size()})
}
