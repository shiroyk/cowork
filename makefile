GO_PROJECT = cowork-auth cowork-collab cowork-user
RUST_PROJECT = cowork-doc
WIRE_PROJECT = cowork-user cowork-collab cowork-auth
PROTO_PROJECT = cowork-doc/api cowork-user/api
.SILENT: proto

default: all

all: gen-proto build-go build-rust build-frontend

test-go:
	for p in $(or $(project),$(GO_PROJECT)); do \
		echo test go project $$p ; \
		go test -race -timeout 60s .$$p/... ;\
	done

build-frontend:
	echo build frontend ; \
	docker buildx build -f dockerfile-frontend --build-arg APP=cowork-frontend -t cowork-frontend:latest . ; \

build-go:
	for p in $(or $(project),$(GO_PROJECT)); do \
		echo build go project $$p ; \
		docker buildx build -f dockerfile-go --build-arg APP=$$p -t $$p:latest . ; \
	done

build-rust:
	for p in $(or $(project),$(RUST_PROJECT)); do \
		echo build rust project $$p ; \
		docker buildx build -f dockerfile-rust --build-arg APP=$$p -t $$p:latest . ; \
	done

gen-wire:
	if ! command -v wire > /dev/null; then \
		echo "wire command is not available, please install wire see https://github.com/google/wire"; \
		exit 1; \
	fi; \
	for p in $(or $(project),$(WIRE_PROJECT)); do \
		echo wire generate project $$p ; \
		cd $$p && wire && cd ..; \
	done

gen-proto:
	for p in $(or $(project),$(PROTO_PROJECT)); do \
		echo generate proto go $$p ; \
		find $$p/proto -name '*.proto' -exec ./scripts/proto_gen.sh {} --go --out=$$p/ ';' ; \
	done

test:

.PHONY: default build gen-proto test