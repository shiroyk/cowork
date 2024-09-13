package client

import (
	common "github.com/shiroyk/cowork/common/golang"
	"github.com/shiroyk/cowork/user/api/generated/golang/api"
	"google.golang.org/grpc"
	"google.golang.org/grpc/credentials/insecure"
)

type Client struct {
	api.UserServiceClient
	conn *grpc.ClientConn
}

func (c *Client) Close() error { return c.conn.Close() }

type UserConfig struct {
	GrpcUser string `yaml:"grpc-user"`
}

func NewClient(cfg UserConfig) *Client {
	target := common.ProfileValue(common.DevGrpcUser, cfg.GrpcUser)
	if target == "" {
		panic("user service unspecified")
	}
	conn, err := grpc.Dial(target, grpc.WithTransportCredentials(insecure.NewCredentials()))
	if err != nil {
		panic(err)
	}
	return &Client{api.NewUserServiceClient(conn), conn}
}
