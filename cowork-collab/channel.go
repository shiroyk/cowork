package main

import (
	"bytes"
	"context"
	"log/slog"
	"net"
	"slices"
	"sync"
	"time"

	"github.com/gobwas/ws/wsutil"
	"github.com/nats-io/nats.go"
	"github.com/redis/go-redis/v9"
	common "github.com/shiroyk/cowork/common/src/main/golang"
	docapi "github.com/shiroyk/cowork/doc/api/src/main/golang/doc/api"
	doc "github.com/shiroyk/cowork/doc/api/src/main/golang/doc/client"
	"github.com/shiroyk/cowork/user/api/src/generated/golang/user/api"
	user "github.com/shiroyk/cowork/user/api/src/main/golang/user/client"
	"github.com/vmihailenco/msgpack/v5"
	"google.golang.org/protobuf/types/known/wrapperspb"
)

// Hub maintains the client connections
type Hub struct {
	sync.Mutex

	// channels did => []*Client
	channels map[string][]*Client

	// clients uid => []*Client
	clients map[string]*Client
}

// Client the socket client.
type Client struct {
	uid, did, rid string

	// conn The websocket connection.
	conn net.Conn
}

// Write data to the connection.
func (c *Client) Write(p []byte) {
	err := wsutil.WriteServerBinary(c.conn, p)
	if err != nil {
		slog.Warn("failed write message", slog.String("error", err.Error()),
			slog.String("user_id", c.uid), slog.String("request_id", c.rid), wsKey)
		return
	}
}

func newHub() *Hub {
	return &Hub{
		channels: make(map[string][]*Client),
		clients:  make(map[string]*Client),
	}
}

// broadcast the channels, if uid not empty, skip the user client
func (h *Hub) broadcast(event docapi.Event, uid, did string, data []byte) {
	h.Lock()
	defer h.Unlock()
	// publish to stream
	if err := nc().PublishMsg(&nats.Msg{Subject: event.Subject(), Data: data, Header: msgHeader}); err != nil {
		slog.Warn("failed publish message", slog.String("error", err.Error()),
			slog.String("user_id", uid), streamKey)
	}
	clients, ok := h.channels[did]
	if !ok {
		return
	}
	for _, client := range clients {
		if len(uid) > 0 && client.uid == uid {
			continue
		}
		client.Write(data)
	}
}

// size total user size
func (h *Hub) size() int { return len(h.clients) }

// login register the client
func (h *Hub) login(client *Client) {
	h.Lock()
	docs := h.channels[client.did]
	h.channels[client.did] = append(docs, client)
	h.clients[client.uid] = client
	h.Unlock()

	{ // online users
		msg := docapi.CollabMessage{Event: docapi.EventLogin, Uid: client.uid, Did: client.did, Data: redisDocUsers(client, actLogin)}
		data, _ := msgpack.Marshal(msg)
		h.broadcast(docapi.EventLogin, "", msg.Did, data)
	}

	{ // sync doc nodes
		msg := docapi.CollabMessage{Event: docapi.EventSync, Uid: client.uid, Did: client.did, Data: docNodes(client)}
		data, _ := msgpack.Marshal(msg)
		client.Write(data)
	}
}

// logout unregister the client
func (h *Hub) logout(client *Client) {
	h.Lock()
	channel, ok := h.channels[client.did]
	if ok {
		h.channels[client.did] = slices.DeleteFunc(channel, func(c *Client) bool { return c == client })
	}
	delete(h.clients, client.uid)
	if err := client.conn.Close(); err != nil {
		slog.Warn("error while close connect", slog.String("error", err.Error()),
			slog.String("user_id", client.uid), slog.String("request_id", client.rid), wsKey)
	}
	h.Unlock()

	// online users
	users := redisDocUsers(client, actLogout)
	if users == nil {
		return
	}
	msg := docapi.CollabMessage{Event: docapi.EventLogout, Uid: client.uid, Did: client.did, Data: users}
	data, _ := msgpack.Marshal(msg)
	h.broadcast(docapi.EventLogout, "", msg.Did, data)
}

type action int

const (
	_ action = iota
	actLogin
	actLogout
)

var actionScript = redis.NewScript(`
local key = KEYS[1]
local uid = ARGV[1]
local act = ARGV[2]
if act == "1" then
	redis.call("SADD", key, uid)
else
	redis.call("SREM", key, uid)
end
return redis.call("SMEMBERS", key)
`)

// redisDocUsers add/remove doc online users from the Redis uid list and return the serialized bytes of users dto.
func redisDocUsers(client *Client, act action) []byte {
	ctx, cancel := context.WithTimeout(context.Background(), time.Second*5)
	defer cancel()
	all, err := actionScript.Run(ctx, common.RedisClient(), []string{client.did}, client.uid, int(act)).StringSlice()
	if err != nil {
		slog.Warn("failed execute online action", slog.String("error", err.Error()),
			slog.String("user_id", client.uid), slog.String("request_id", client.rid), wsKey)
		return nil
	}
	if len(all) == 0 {
		return nil
	}
	users, err := user.UserServiceClient().FindByIds(ctx, &api.Ids{Id: all})
	if err != nil {
		slog.Warn("failed get users", slog.String("error", err.Error()),
			slog.String("user_id", client.uid), slog.String("request_id", client.rid), grpcKey)
		return nil
	}

	enc := msgpack.GetEncoder()
	defer msgpack.PutEncoder(enc)

	var buf bytes.Buffer
	enc.Reset(&buf)
	enc.SetCustomStructTag("json") // use the json tag
	if err = enc.Encode(users.Item); err != nil {
		slog.Warn("failed marshal users message", slog.String("error", err.Error()),
			slog.String("user_id", client.uid), slog.String("request_id", client.rid), wsKey)
		return nil
	}

	return buf.Bytes()
}

// docNodes get all doc nodes
func docNodes(client *Client) []byte {
	ctx, cancel := context.WithTimeout(context.Background(), time.Second*10)
	defer cancel()
	nodes, err := doc.DocServiceClient().FindNodesByDid(ctx, wrapperspb.String(client.did))
	if err != nil {
		slog.Warn("failed get doc nodes", slog.String("error", err.Error()),
			slog.String("user_id", client.uid), slog.String("request_id", client.rid), grpcKey)
		return nil
	}
	data, err := msgpack.Marshal(nodes.Nodes)
	if err != nil {
		slog.Warn("failed marshal doc nodes", slog.String("error", err.Error()),
			slog.String("user_id", client.uid), slog.String("request_id", client.rid), wsKey)
		return nil
	}
	return data
}
