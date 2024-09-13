package client

import (
	common "github.com/shiroyk/cowork/common/golang"
	"github.com/shiroyk/cowork/doc/api/generated/golang/api"
	"google.golang.org/grpc"
	"google.golang.org/grpc/credentials/insecure"
)

type Client struct {
	api.DocServiceClient
	conn *grpc.ClientConn
}

func (c *Client) Close() error { return c.conn.Close() }

type DocConfig struct {
	GrpcDoc string `yaml:"grpc-doc"`
}

func NewClient(cfg DocConfig) *Client {
	target := common.ProfileValue(common.DevGrpcDoc, cfg.GrpcDoc)
	if target == "" {
		panic("doc service unspecified")
	}
	conn, err := grpc.Dial(target, grpc.WithTransportCredentials(insecure.NewCredentials()))
	if err != nil {
		panic(err)
	}
	return &Client{api.NewDocServiceClient(conn), conn}
}
