package main

import (
	"log/slog"
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/gobwas/ws"
	"github.com/gobwas/ws/wsutil"
	"github.com/nats-io/nats.go"
	common "github.com/shiroyk/cowork/common/src/main/golang"
	docapi "github.com/shiroyk/cowork/doc/api/src/main/golang/doc/api"
	"github.com/vmihailenco/msgpack/v5"
)

func init() {
	router.GET("/api/:did", wsHandle)
}

// wsHandle websocket requests from the peer.
func wsHandle(ctx *gin.Context) {
	if !ctx.IsWebsocket() {
		ctx.JSON(http.StatusBadRequest, common.NewErrorMessage(http.StatusText(http.StatusBadRequest)))
		return
	}
	requestId := ctx.GetHeader("X-Request-ID")
	uid := ctx.GetHeader("X-User-Id")

	conn, _, _, err := (ws.HTTPUpgrader{
		Header: map[string][]string{
			"Sec-WebSocket-Protocol": {ctx.Request.Header.Get("Sec-WebSocket-Protocol")},
		},
	}).Upgrade(ctx.Request, ctx.Writer)
	if err != nil {
		ctx.JSON(http.StatusBadRequest, common.NewErrorMessage(err.Error()))
		return
	}

	client := &Client{uid, ctx.Param("did"), requestId, conn}
	go func() {
		defer hub.logout(client)

		hub.login(client)
		var data []byte

		for {
			data, _, err = wsutil.ReadClientData(conn)
			if err != nil {
				break
			}
			var msg docapi.CollabMessage
			if err = msgpack.Unmarshal(data, &msg); err != nil {
				slog.Warn("failed marshal client message", slog.String("error", err.Error()),
					slog.String("user_id", client.uid), slog.String("request_id", client.rid), wsKey)
				break
			}

			// publish to eventSubcribe
			if err = nc().PublishMsg(&nats.Msg{Subject: msg.Event.Subject(), Data: data, Header: msgHeader}); err != nil {
				slog.Warn("failed publish message", slog.String("error", err.Error()),
					slog.String("user_id", client.uid), slog.String("request_id", client.rid), streamKey)
			}
			hub.broadcast(msg.Uid, msg.Did, data)
		}
	}()
}
