package main

import (
	"fmt"
	"log/slog"
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/gobwas/ws"
	"github.com/gobwas/ws/wsutil"
	common "github.com/shiroyk/cowork/common/golang"
	"github.com/shiroyk/cowork/doc/api/golang/event"
	"github.com/vmihailenco/msgpack/v5"
)

func (hub *Hub) router(eng *gin.Engine) {
	eng.GET("/api/:did", hub.wsHandle)
	eng.Any("/metrics", hub.metrics)
}

func (hub *Hub) metrics(ctx *gin.Context) {
	ctx.JSON(http.StatusOK, map[string]any{"users": hub.size()})
}

// wsHandle websocket requests from the peer.
func (hub *Hub) wsHandle(ctx *gin.Context) {
	if !ctx.IsWebsocket() {
		ctx.JSON(http.StatusBadRequest, common.NewErrorMessage(http.StatusText(http.StatusBadRequest)))
		return
	}
	requestId := ctx.GetHeader(common.HeaderRequestID)
	uid := ctx.Request.Header.Get(common.HeaderUserID)
	protocol := ctx.Request.Header.Get("Sec-WebSocket-Protocol")

	conn, _, _, err := (ws.HTTPUpgrader{
		Header: map[string][]string{
			"Sec-WebSocket-Protocol": {protocol},
		},
	}).Upgrade(ctx.Request, ctx.Writer)
	if err != nil {
		ctx.JSON(http.StatusBadRequest, common.NewErrorMessage(err.Error()))
		return
	}

	// TODO: token expired

	client := &Client{uid, ctx.Param("did"), requestId, conn}
	go func() {
		defer hub.logout(client)

		hub.login(client)

		for {
			data, op, err := wsutil.ReadClientData(conn)
			if err != nil {
				break
			}

			switch op {
			case ws.OpClose:
				return
			case ws.OpPing:
				err = wsutil.WriteServerMessage(conn, ws.OpPong, nil)
				if err != nil {
					slog.Warn("failed write pong message", slog.String("error", err.Error()),
						slog.String("user_id", client.uid), slog.String("request_id", client.rid), keyWS)
				}
				continue
			}

			if op != ws.OpBinary {
				continue
			}

			var msg event.CollabMessage
			if err = msgpack.Unmarshal(data, &msg); err != nil {
				slog.Warn("failed marshal client message", slog.String("error", err.Error()),
					slog.String("user_id", client.uid), slog.String("request_id", client.rid), keyWS)
				_ = wsutil.WriteServerMessage(conn, ws.OpClose, []byte(fmt.Sprintf("invalid message: %s", err.Error())))
				break
			}

			msg.Uid = client.uid
			msg.Did = client.did
			hub.broadcast(client.uid, msg)
		}
	}()
}
