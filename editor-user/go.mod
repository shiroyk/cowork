module github.com/shiroyk/crdt-editor/user

go 1.22.0

toolchain go1.23.0

require (
	github.com/gin-gonic/gin v1.9.1
	github.com/google/wire v0.6.0
	github.com/shiroyk/crdt-editor/common v0.0.0
	github.com/shiroyk/crdt-editor/user/api v0.0.0
	google.golang.org/grpc v1.62.1
	google.golang.org/protobuf v1.33.0
	gorm.io/driver/mysql v1.5.7
	gorm.io/gorm v1.25.12
)

require (
	filippo.io/edwards25519 v1.1.0 // indirect
	github.com/bytedance/sonic v1.10.2 // indirect
	github.com/cespare/xxhash/v2 v2.2.0 // indirect
	github.com/chenzhuoyu/base64x v0.0.0-20230717121745-296ad89f973d // indirect
	github.com/chenzhuoyu/iasm v0.9.1 // indirect
	github.com/dgryski/go-rendezvous v0.0.0-20200823014737-9f7001d12a5f // indirect
	github.com/fsnotify/fsnotify v1.7.0 // indirect
	github.com/gabriel-vasile/mimetype v1.4.3 // indirect
	github.com/gin-contrib/sse v0.1.0 // indirect
	github.com/go-playground/locales v0.14.1 // indirect
	github.com/go-playground/universal-translator v0.18.1 // indirect
	github.com/go-playground/validator/v10 v10.16.0 // indirect
	github.com/go-sql-driver/mysql v1.8.1 // indirect
	github.com/goccy/go-json v0.10.2 // indirect
	github.com/golang/protobuf v1.5.3 // indirect
	github.com/jinzhu/inflection v1.0.0 // indirect
	github.com/jinzhu/now v1.1.5 // indirect
	github.com/json-iterator/go v1.1.12 // indirect
	github.com/klauspost/cpuid/v2 v2.2.6 // indirect
	github.com/leodido/go-urn v1.2.4 // indirect
	github.com/mattn/go-isatty v0.0.20 // indirect
	github.com/modern-go/concurrent v0.0.0-20180306012644-bacd9c7ef1dd // indirect
	github.com/modern-go/reflect2 v1.0.2 // indirect
	github.com/pelletier/go-toml/v2 v2.2.3 // indirect
	github.com/redis/go-redis/v9 v9.2.1 // indirect
	github.com/sony/sonyflake v1.2.0 // indirect
	github.com/twitchyliquid64/golang-asm v0.15.1 // indirect
	github.com/ugorji/go/codec v1.2.12 // indirect
	golang.org/x/arch v0.6.0 // indirect
	golang.org/x/crypto v0.27.0 // indirect
	golang.org/x/net v0.23.0 // indirect
	golang.org/x/sys v0.25.0 // indirect
	golang.org/x/text v0.18.0 // indirect
	google.golang.org/genproto/googleapis/rpc v0.0.0-20240314234333-6e1732d8331c // indirect
	gopkg.in/check.v1 v1.0.0-20201130134442-10cb98267c6c // indirect
	gopkg.in/yaml.v3 v3.0.1 // indirect
)

replace (
	github.com/shiroyk/crdt-editor/common v0.0.0 => ../editor-common
	github.com/shiroyk/crdt-editor/user/api v0.0.0 => ./api
)
