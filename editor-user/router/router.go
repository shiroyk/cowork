package router

import (
	"net/http"
	"strconv"

	"github.com/gin-gonic/gin"
	common "github.com/shiroyk/crdt-editor/common/golang"
	. "github.com/shiroyk/crdt-editor/user/model"
	"github.com/shiroyk/crdt-editor/user/service"
)

type Router struct {
	serv service.UserService
}

func NewRouter(impl service.UserService) *Router {
	return &Router{impl}
}

func (r *Router) Enable(engine *gin.Engine) {
	engine.GET("/api", r.searchUser)
	engine.GET("/api/:id", r.getUser)
	engine.PUT("/api", r.saveUser)
}

// searchUser search users
func (r *Router) searchUser(ctx *gin.Context) {
	var query UserQuery
	if err := ctx.ShouldBindQuery(&query); err != nil {
		ctx.JSON(http.StatusBadRequest, common.NewErrorMessage(err.Error()))
		return
	}
	users, size, err := r.serv.Search(ctx, query)
	if err != nil {
		ctx.JSON(http.StatusInternalServerError, common.NewErrorMessage(err.Error()))
	}

	ctx.Header("X-Total-Count", strconv.FormatInt(size, 10))
	ctx.JSON(http.StatusOK, users)
}

// getUser get user
func (r *Router) getUser(ctx *gin.Context) {
	var (
		id  uint64
		err error
	)

	if s := ctx.Param("id"); s != "" {
		id, err = strconv.ParseUint(s, 10, 64)
		if err != nil {
			ctx.JSON(http.StatusBadRequest, common.NewErrorMessage(err.Error()))
			return
		}
	}
	user, err := r.serv.Get(ctx, id)
	if err != nil {
		ctx.JSON(http.StatusInternalServerError, common.NewErrorMessage(err.Error()))
		return
	}
	ctx.JSON(http.StatusOK, user)
}

// saveUser save user
func (r *Router) saveUser(ctx *gin.Context) {
	var u User
	if err := ctx.ShouldBindJSON(&u); err != nil {
		ctx.JSON(http.StatusBadRequest, common.NewErrorMessage(err.Error()))
		return
	}
	if err := r.serv.Save(ctx, u); err != nil {
		ctx.JSON(http.StatusInternalServerError, common.NewErrorMessage(err.Error()))
		return
	}
	ctx.Status(http.StatusNoContent)
}
