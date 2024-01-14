package common

import (
	"net/http"

	"google.golang.org/grpc/codes"
)

// Permission the project permissions
type Permission uint8

const (
	PermissionEmpty Permission = iota
	PermissionRead
	PermissionComment
	PermissionWrite
)

// ErrorMessage the error message
type ErrorMessage struct {
	Error string `json:"error"`
}

// NewErrorMessage returns a new ErrorMessage
func NewErrorMessage(message string) ErrorMessage { return ErrorMessage{Error: message} }

// ApiError the API error
type ApiError struct {
	Code    int
	Message string
}

// NewApiError returns a new ApiError
func NewApiError(code int) error { return &ApiError{Code: code} }

// NewBadRequestApiError returns a new http.StatusBadRequest ApiError
func NewBadRequestApiError(message string) error {
	return &ApiError{Code: http.StatusBadRequest, Message: message}
}

// Error returns the error message
func (e *ApiError) Error() string {
	if e.Message != "" {
		return e.Message
	}
	return http.StatusText(e.Code)
}

// GrpcCodeToHttpStatus converts grpc code to the http.StatusCode
func GrpcCodeToHttpStatus(code codes.Code) int {
	switch code {
	case codes.Canceled, codes.Unknown, codes.ResourceExhausted,
		codes.FailedPrecondition, codes.Aborted, codes.OutOfRange,
		codes.Internal, codes.Unimplemented, codes.Unavailable:
		return http.StatusInternalServerError
	case codes.InvalidArgument:
		return http.StatusBadRequest
	case codes.DeadlineExceeded:
		return http.StatusRequestTimeout
	case codes.NotFound:
		return http.StatusNotFound
	case codes.AlreadyExists:
		return http.StatusConflict
	case codes.PermissionDenied:
		return http.StatusForbidden
	case codes.DataLoss:
		return http.StatusNotFound
	case codes.Unauthenticated:
		return http.StatusForbidden
	}
	return http.StatusOK
}
