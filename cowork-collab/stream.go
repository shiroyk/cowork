package main

import (
	"os"
	"sync"

	"github.com/nats-io/nats.go"
	common "github.com/shiroyk/cowork/common/src/main/golang"
	docapi "github.com/shiroyk/cowork/doc/api/src/main/golang/doc/api"
	"github.com/vmihailenco/msgpack/v5"
)

var nc = sync.OnceValue[*nats.Conn](func() *nats.Conn {
	nc, err := nats.Connect(common.ProfileValue("nats://localhost:4222", os.Getenv("NATS_URL")))
	if err != nil {
		panic(err)
	}
	return nc
})

var (
	hostname  = common.ProfileValue("dev", os.Getenv("HOSTNAME"))
	msgHeader = nats.Header{docapi.HeaderSource: {hostname}}
)

func subscribe() *nats.Subscription {
	return common.Must1(nc().Subscribe("events.*", func(msg *nats.Msg) {
		if msg.Header.Get(docapi.HeaderSource) == hostname {
			// ignore msg from local
			return
		}
		var cm docapi.CollabMessage
		err := msgpack.Unmarshal(msg.Data, &cm)
		if err != nil {
			return
		}

		// consume the message from stream
		switch cm.Event {
		case docapi.EventSave:
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
	}))
}
