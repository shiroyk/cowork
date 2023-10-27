GO_PROJECT = cowork-auth cowork-collab
SPRING_PROJECT = cowork-user cowork-doc
PROTO_PROJECT = cowork-doc/api cowork-user/api
.SILENT: proto

default: all

all: proto test build-spring-native build-go build-frontend

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

build-spring:
	for p in $(or $(project),$(SPRING_PROJECT)); do \
		echo build spring project $$p; \
		docker buildx build -f dockerfile-spring --build-arg APP=$$p -t $$p:latest . ; \
	done

build-spring-native:
	for p in $(or $(project),$(SPRING_PROJECT)); do \
		echo build spring native project $$p; \
		docker buildx build -f dockerfile-spring-native --build-arg APP=$$p -t $$p:latest . ; \
	done

proto:
	for p in $(or $(project),$(PROTO_PROJECT)); do \
		echo generate proto $(or $(gen),all) $$p ; \
		find $$p/src -name '*.proto' -exec ./scripts/proto_gen.sh {} --$(or $(gen),all) --out=$$p/src ';' ; \
	done

test:

.PHONY: default build proto test