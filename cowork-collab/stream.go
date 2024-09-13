package main

import (
	"fmt"
	"os"

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

		// consume the message from stream
		switch cm.Event {
		case event.Save, event.Sync:
			client, ok := hub.clients[cm.Uid]
			if ok {
				client.Write(msg.Data)
			}
		default:
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
