package common

import (
	"context"
	"net/http"

	"google.golang.org/grpc/metadata"
)

// RequestMetadata returns the context with metadata user_id and request_id.
func RequestMetadata(req *http.Request) context.Context {
	kvs := make([]string, 0, 4)
	kvs = append(kvs, "request_id", req.Header.Get("X-Request-ID"))
	if _, ok := req.Header["X-User-Id"]; ok {
		kvs = append(kvs, "user_id", req.Header.Get("X-User-Id"))
	}
	id := metadata.Pairs(kvs...)
	return metadata.NewOutgoingContext(req.Context(), id)
}
