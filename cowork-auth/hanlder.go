package main

import (
	"errors"
	"net/http"

	"github.com/labstack/echo/v4"
	common "github.com/shiroyk/cowork/common/src/main/golang"
	"google.golang.org/grpc/status"
)

// errorHandler handles the error
func errorHandler(err error, c echo.Context) {
	var code = http.StatusInternalServerError
	var message = err.Error()

	{
		var apiErr *common.ApiError
		ok := errors.As(err, &apiErr)
		if ok {
			code = apiErr.Code
			goto ret
		}
	}
	{
		grpcErr, ok := status.FromError(err)
		if ok {
			code = common.GrpcCodeToHttpStatus(grpcErr.Code())
			message = grpcErr.Message()
			goto ret
		}
	}
	{
		c.Logger().Errorf("request_id %s URI %s method %s %s",
			c.Request().Header.Get("X-Request-ID"), c.Request().Method, c.Request().RequestURI, err)
	}

ret:
	_ = c.JSON(code, common.NewErrorMessage(message))
}
