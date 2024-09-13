module github.com/shiroyk/cowork/user/api

go 1.22.0

toolchain go1.23.0

require (
	github.com/shiroyk/cowork/common v0.0.0
	google.golang.org/grpc v1.62.1
	google.golang.org/protobuf v1.33.0
)

require (
	github.com/cespare/xxhash/v2 v2.2.0 // indirect
	github.com/dgryski/go-rendezvous v0.0.0-20200823014737-9f7001d12a5f // indirect
	github.com/fsnotify/fsnotify v1.7.0 // indirect
	github.com/golang/protobuf v1.5.3 // indirect
	github.com/kr/text v0.2.0 // indirect
	github.com/redis/go-redis/v9 v9.2.1 // indirect
	github.com/rogpeppe/go-internal v1.12.0 // indirect
	github.com/sony/sonyflake v1.2.0 // indirect
	golang.org/x/net v0.23.0 // indirect
	golang.org/x/sys v0.25.0 // indirect
	golang.org/x/text v0.18.0 // indirect
	google.golang.org/genproto/googleapis/rpc v0.0.0-20240314234333-6e1732d8331c // indirect
	gopkg.in/yaml.v3 v3.0.1 // indirect
)

replace github.com/shiroyk/cowork/common v0.0.0 => ../../cowork-common
