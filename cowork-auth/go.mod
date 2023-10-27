module github.com/shiroyk/cowork/auth

go 1.21

require (
	github.com/google/uuid v1.3.1
	github.com/labstack/echo/v4 v4.11.2
	github.com/labstack/gommon v0.4.0
	github.com/shiroyk/cowork/common v0.0.0
	github.com/shiroyk/cowork/user/api v0.0.0
	golang.org/x/crypto v0.14.0
	google.golang.org/grpc v1.58.3
	google.golang.org/protobuf v1.31.0
)

require (
	github.com/cespare/xxhash/v2 v2.2.0 // indirect
	github.com/dgryski/go-rendezvous v0.0.0-20200823014737-9f7001d12a5f // indirect
	github.com/golang-jwt/jwt v3.2.2+incompatible // indirect
	github.com/golang-jwt/jwt/v5 v5.0.0 // indirect
	github.com/golang/protobuf v1.5.3 // indirect
	github.com/mattn/go-colorable v0.1.13 // indirect
	github.com/mattn/go-isatty v0.0.19 // indirect
	github.com/redis/go-redis/v9 v9.2.1 // indirect
	github.com/valyala/bytebufferpool v1.0.0 // indirect
	github.com/valyala/fasttemplate v1.2.2 // indirect
	golang.org/x/net v0.17.0 // indirect
	golang.org/x/sys v0.13.0 // indirect
	golang.org/x/text v0.13.0 // indirect
	golang.org/x/time v0.3.0 // indirect
	google.golang.org/genproto/googleapis/rpc v0.0.0-20230711160842-782d3b101e98 // indirect
)

replace (
	github.com/shiroyk/cowork/common v0.0.0 => ../cowork-common
	github.com/shiroyk/cowork/user/api v0.0.0 => ../cowork-user/api
)
