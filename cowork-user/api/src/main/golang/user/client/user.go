package client

import (
	"os"
	"sync"

	common "github.com/shiroyk/cowork/common/src/main/golang"
	"github.com/shiroyk/cowork/user/api/src/generated/golang/user/api"
	"google.golang.org/grpc"
	"google.golang.org/grpc/credentials/insecure"
)

type Client struct {
	api.UserServiceClient
	conn *grpc.ClientConn
}

func (c *Client) Close() error { return c.conn.Close() }

// UserServiceClient the GRPC user service client instance.
var UserServiceClient = sync.OnceValue(func() *Client {
	target := common.ProfileValue("localhost:9091", os.Getenv("USER_GRPC_SERVICE"))
	if target == "" {
		panic("user service unspecified")
	}
	conn, err := grpc.Dial(target, grpc.WithTransportCredentials(insecure.NewCredentials()))
	if err != nil {
		panic(err)
	}
	return &Client{api.NewUserServiceClient(conn), conn}
})
