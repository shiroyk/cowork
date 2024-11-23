module github.com/shiroyk/crdt-editor/auth

go 1.22.0

toolchain go1.23.0

require (
	github.com/golang-jwt/jwt/v5 v5.2.1
	github.com/google/uuid v1.6.0
	github.com/google/wire v0.6.0
	github.com/labstack/echo/v4 v4.11.2
	github.com/labstack/gommon v0.4.0
	github.com/redis/go-redis/v9 v9.2.1
	github.com/shiroyk/crdt-editor/common v0.0.0
	github.com/shiroyk/crdt-editor/user/api v0.0.0
	golang.org/x/crypto v0.27.0
	google.golang.org/grpc v1.62.1
	google.golang.org/protobuf v1.34.2
)

require (
	github.com/cespare/xxhash/v2 v2.3.0 // indirect
	github.com/dgryski/go-rendezvous v0.0.0-20200823014737-9f7001d12a5f // indirect
	github.com/fsnotify/fsnotify v1.7.0 // indirect
	github.com/golang-jwt/jwt v3.2.2+incompatible // indirect
	github.com/golang/protobuf v1.5.4 // indirect
	github.com/mattn/go-colorable v0.1.13 // indirect
	github.com/mattn/go-isatty v0.0.20 // indirect
	github.com/sony/sonyflake v1.2.0 // indirect
	github.com/valyala/bytebufferpool v1.0.0 // indirect
	github.com/valyala/fasttemplate v1.2.2 // indirect
	golang.org/x/net v0.29.0 // indirect
	golang.org/x/sys v0.25.0 // indirect
	golang.org/x/text v0.18.0 // indirect
	golang.org/x/time v0.6.0 // indirect
	google.golang.org/genproto/googleapis/rpc v0.0.0-20240903143218-8af14fe29dc1 // indirect
	gopkg.in/yaml.v3 v3.0.1 // indirect
)

replace (
	github.com/shiroyk/crdt-editor/common v0.0.0 => ../editor-common
	github.com/shiroyk/crdt-editor/user/api v0.0.0 => ../editor-user/api
)
