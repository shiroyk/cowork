package client

import (
	"os"
	"sync"

	common "github.com/shiroyk/cowork/common/src/main/golang"
	"github.com/shiroyk/cowork/doc/api/src/generated/golang/doc/api"
	"google.golang.org/grpc"
	"google.golang.org/grpc/credentials/insecure"
)

type Client struct {
	api.DocServiceClient
	conn *grpc.ClientConn
}

func (c *Client) Close() error { return c.conn.Close() }

// DocServiceClient the GRPC doc service client instance.
var DocServiceClient = sync.OnceValue(func() *Client {
	target := common.ProfileValue("localhost:9094", os.Getenv("DOC_GRPC_SERVICE"))
	if target == "" {
		panic("doc service unspecified")
	}
	conn, err := grpc.Dial(target, grpc.WithTransportCredentials(insecure.NewCredentials()))
	if err != nil {
		panic(err)
	}
	return &Client{api.NewDocServiceClient(conn), conn}
})
