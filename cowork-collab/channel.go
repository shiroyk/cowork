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
	"github.com/shiroyk/cowork/doc/api/generated/golang/api"
	docclient "github.com/shiroyk/cowork/doc/api/golang/client"
	"github.com/shiroyk/cowork/doc/api/golang/event"
	userapi "github.com/shiroyk/cowork/user/api/generated/golang/api"
	userclient "github.com/shiroyk/cowork/user/api/golang/client"
	"github.com/vmihailenco/msgpack/v5"
	"google.golang.org/protobuf/types/known/wrapperspb"
)

// Hub maintains the client connections
type Hub struct {
	sync.RWMutex
	redis redis.UniversalClient
	nats  *nats.Conn
	doc   *docclient.Client
	user  *userclient.Client

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
			slog.String("user_id", c.uid), slog.String("request_id", c.rid), keyWS)
		return
	}
}

func newHub(
	doc *docclient.Client,
	nats *nats.Conn,
	user *userclient.Client,
	redis redis.UniversalClient,
) (*Hub, func()) {
	h := &Hub{
		doc:      doc,
		nats:     nats,
		user:     user,
		redis:    redis,
		channels: make(map[string][]*Client),
		clients:  make(map[string]*Client),
	}
	subscribe := h.subscribe()
	return h, func() {
		subscribe.Unsubscribe()
		h.Lock()
		defer h.Unlock()
		for _, client := range h.clients {
			client.conn.Close()
		}
	}
}

// broadcast the channels, skip the user client if skip not empty
func (hub *Hub) broadcast(skip string, msg event.CollabMessage) {
	hub.RLock()
	defer hub.RUnlock()
	slog.Debug("broadcast", slog.String("event", msg.Event.String()), slog.String("did", msg.Did), slog.String("uid", msg.Uid), keyStream)
	// publish to stream
	data, _ := msgpack.Marshal(msg)
	if err := hub.nats.PublishMsg(&nats.Msg{Subject: msg.Event.Subject(), Data: data, Header: msgHeader}); err != nil {
		slog.Warn("failed publish message", slog.String("error", err.Error()),
			slog.String("user_id", msg.Uid), keyStream)
	}
	clients, ok := hub.channels[msg.Did]
	if !ok {
		return
	}
	for _, client := range clients {
		if len(skip) > 0 && client.uid == skip {
			continue
		}
		client.Write(data)
	}
}

// size total user size
func (hub *Hub) size() int { return len(hub.clients) }

// login register the client
func (hub *Hub) login(client *Client) {
	hub.Lock()
	docs := hub.channels[client.did]
	hub.channels[client.did] = append(docs, client)
	hub.clients[client.uid] = client
	hub.Unlock()
	slog.Debug("user login", slog.String("user_id", client.uid),
		slog.String("did", client.did), slog.String("request_id", client.rid), keyWS)

	{ // sync doc nodes
		msg := event.CollabMessage{Event: event.Sync, Uid: client.uid, Did: client.did, Data: hub.docContent(client)}
		data, _ := msgpack.Marshal(msg)
		client.Write(data)
	}

	{ // online users
		msg := event.CollabMessage{Event: event.Login, Uid: client.uid, Did: client.did, Data: hub.onlineDocUsers(client, actLogin)}
		hub.broadcast("", msg)
	}
}

// logout unregister the client
func (hub *Hub) logout(client *Client) {
	hub.Lock()
	channel, ok := hub.channels[client.did]
	if ok {
		hub.channels[client.did] = slices.DeleteFunc(channel, func(c *Client) bool { return c == client })
	}
	slog.Debug("user logout", slog.String("user_id", client.uid),
		slog.String("did", client.did), slog.String("request_id", client.rid), keyWS)
	delete(hub.clients, client.uid)
	if err := client.conn.Close(); err != nil {
		slog.Warn("error while close connect", slog.String("error", err.Error()),
			slog.String("user_id", client.uid), slog.String("request_id", client.rid), keyWS)
	}
	hub.Unlock()

	// online users
	users := hub.onlineDocUsers(client, actLogout)
	if users == nil {
		return
	}
	msg := event.CollabMessage{Event: event.Logout, Uid: client.uid, Did: client.did, Data: users}
	hub.broadcast("", msg)
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

type onlineUser struct {
	*userapi.UserList_Dto
	ClientID int32 `json:"client_id"`
}

// onlineDocUsers add/remove doc online users from the Redis uid list and return the serialized bytes of users dto.
func (hub *Hub) onlineDocUsers(client *Client, act action) []byte {
	ctx, cancel := context.WithTimeout(context.Background(), time.Second*5)
	defer cancel()
	all, err := actionScript.Run(ctx, hub.redis, []string{client.did}, client.uid, int(act)).StringSlice()
	if err != nil {
		slog.Warn("failed execute online action", slog.String("error", err.Error()),
			slog.String("user_id", client.uid), slog.String("request_id", client.rid), keyWS)
		return nil
	}
	if len(all) == 0 {
		return nil
	}
	users, err := hub.user.FindByIds(ctx, &userapi.Ids{Id: all})
	if err != nil {
		slog.Warn("failed get users", slog.String("error", err.Error()),
			slog.String("user_id", client.uid), slog.String("request_id", client.rid), keyGRPC)
		return nil
	}

	doc, err := hub.doc.FindDoc(ctx, &wrapperspb.StringValue{Value: client.did})
	if err != nil {
		slog.Warn("failed get doc", slog.String("error", err.Error()),
			slog.String("user_id", client.uid), slog.String("request_id", client.rid), keyGRPC)
		return nil
	}

	onlineUsers := make([]onlineUser, 0, len(all))
	for _, user := range users.Item {
		onlineUsers = append(onlineUsers, onlineUser{
			UserList_Dto: user,
			ClientID:     doc.Clients[user.Id],
		})
	}

	data, err := marshalWithTag(onlineUsers)
	if err != nil {
		slog.Warn("failed marshal users message", slog.String("error", err.Error()),
			slog.String("user_id", client.uid), slog.String("request_id", client.rid), keyWS)
		return nil
	}

	return data
}

// docContent get all doc content
func (hub *Hub) docContent(client *Client) []byte {
	ctx, cancel := context.WithTimeout(context.Background(), time.Second*10)
	defer cancel()
	content, err := hub.doc.FindContent(ctx, &api.DocContentReq{Did: client.did, Uid: client.uid})
	if err != nil {
		slog.Warn("failed get doc content", slog.String("error", err.Error()),
			slog.String("user_id", client.uid), slog.String("request_id", client.rid), keyGRPC)
		return nil
	}
	data, err := marshalWithTag(content)
	if err != nil {
		slog.Warn("failed marshal doc content", slog.String("error", err.Error()),
			slog.String("user_id", client.uid), slog.String("request_id", client.rid), keyWS)
		return nil
	}
	return data
}

func marshalWithTag(v any) ([]byte, error) {
	enc := msgpack.GetEncoder()
	defer msgpack.PutEncoder(enc)

	var buf bytes.Buffer
	enc.Reset(&buf)
	enc.SetCustomStructTag("json") // use the json tag
	err := enc.Encode(v)
	return buf.Bytes(), err
}
