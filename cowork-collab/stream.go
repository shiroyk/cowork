package main

import (
	"fmt"
	"os"
	"slices"

	"github.com/gobwas/ws"
	"github.com/gobwas/ws/wsutil"
	"github.com/nats-io/nats.go"
	common "github.com/shiroyk/cowork/common/golang"
	"github.com/shiroyk/cowork/doc/api/golang/event"
	"github.com/vmihailenco/msgpack/v5"
)

func newNats(cfg config) (*nats.Conn, func(), error) {
	nc, err := nats.Connect(cfg.Nats)
	if err != nil {
		return nil, nil, fmt.Errorf("failed to connect nats: %w", err)
	}

	return nc, func() { nc.Drain() }, nil
}

var (
	hostname  = common.ProfileValue("collab-dev", os.Getenv("HOSTNAME"))
	msgHeader = nats.Header{event.HeaderSource: {hostname}}
)

func (hub *Hub) subscribe() *nats.Subscription {
	sub, err := hub.nats.Subscribe("events.*", func(msg *nats.Msg) {
		if msg.Header.Get(event.HeaderSource) == hostname {
			// ignore msg from local
			return
		}
		var cm event.CollabMessage
		err := msgpack.Unmarshal(msg.Data, &cm)
		if err != nil {
			return
		}

		hub.RLock()
		defer hub.RUnlock()

		// consume the message from stream
		switch cm.Event {
		case event.Save, event.Sync:
			client, ok := hub.clients[cm.Uid]
			if ok {
				client.Write(msg.Data)
			}
		default:
			if event.Login == cm.Event {
				// login on another device
				if client, ok := hub.clients[cm.Uid]; ok {
					hub.RUnlock()
					hub.Lock()
					defer hub.Unlock()
					wsutil.WriteServerMessage(client.conn, ws.OpClose, []byte("user logged in on another device"))
					delete(hub.clients, cm.Uid)
					channel, ok := hub.channels[client.did]
					if ok {
						hub.channels[client.did] = slices.DeleteFunc(channel, func(c *Client) bool { return c == client })
					}
					client.conn.Close()
					return
				}
			}
			clients, ok := hub.channels[cm.Did]
			if !ok {
				return
			}
			for _, client := range clients {
				client.Write(msg.Data)
			}
		}
	})

	if err != nil {
		panic(fmt.Errorf("failed to subscribe events: %w", err))
	}

	return sub
}
