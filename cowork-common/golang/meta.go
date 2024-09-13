package common

import (
	"context"
	"net/http"

	"google.golang.org/grpc/metadata"
)

const (
	HeaderRequestID = "X-Request-ID"
	HeaderUserID    = "X-User-ID"
	keyRequestID    = "Cowork-request-id"
	keyUserID       = "Cowork-user-id"
)

// GrpcMetadata returns the context with metadata user_id and request_id.
func GrpcMetadata(req *http.Request) context.Context {
	kvs := make([]string, 0, 4)
	kvs = append(kvs, keyRequestID, req.Header.Get(HeaderRequestID))
	if id, ok := req.Header[HeaderUserID]; ok && len(id) > 0 {
		kvs = append(kvs, keyUserID, id[0])
	}
	id := metadata.Pairs(kvs...)
	return metadata.NewOutgoingContext(req.Context(), id)
}

// RequestMetadata returns the context with metadata user_id and request_id.
func RequestMetadata(req *http.Request) context.Context {
	ctx := context.WithValue(req.Context(), keyRequestID, req.Header.Get(HeaderRequestID))
	if id, ok := req.Header[HeaderUserID]; ok && len(id) > 0 {
		return context.WithValue(ctx, keyUserID, id[0])
	}
	return ctx
}

func UserIdFromContext(ctx context.Context) (string, bool) {
	s, ok := ctx.Value(keyUserID).(string)
	return s, ok
}
