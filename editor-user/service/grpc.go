package service

import (
	"context"
	"errors"
	"fmt"
	"net/http"
	"strconv"

	common "github.com/shiroyk/crdt-editor/common/golang"
	api "github.com/shiroyk/crdt-editor/user/api/generated/golang/api"
	. "github.com/shiroyk/crdt-editor/user/model"
	"google.golang.org/protobuf/types/known/emptypb"
	"google.golang.org/protobuf/types/known/wrapperspb"
	"gorm.io/gorm"
)

var errUserNotFound = common.ApiError{
	Code:    http.StatusNotFound,
	Message: "user not found",
}

type GrpcService struct {
	api.UserServiceServer
	db *gorm.DB
}

func NewGrpcService(db *gorm.DB) api.UserServiceServer {
	return &GrpcService{db: db}
}

func (g *GrpcService) FindById(ctx context.Context, value *wrapperspb.StringValue) (*api.User, error) {
	user := new(UserInfo)
	err := g.db.Table(TableUser).WithContext(ctx).
		Find(user, "id = ?", value.Value).Error
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, errUserNotFound
		}
		return nil, fmt.Errorf("failed to get user: %w", err)
	}

	ret := &api.User{
		Id:       user.Id,
		Username: user.Username,
		Email:    user.Email,
		Password: user.Password,
	}
	g.sessions(ctx, ret)
	return ret, nil
}

func (g *GrpcService) FindByIds(ctx context.Context, ids *api.Ids) (*api.UserList, error) {
	var users []*api.UserList_Dto

	err := g.db.Table(TableUser).WithContext(ctx).
		Where("id in ?", ids.Id).
		Find(&users).Error
	if err != nil {
		return nil, fmt.Errorf("failed to get users: %w", err)
	}
	return &api.UserList{Item: users}, nil
}

func (g *GrpcService) FindByName(ctx context.Context, value *wrapperspb.StringValue) (*api.User, error) {
	user := new(UserInfo)
	err := g.db.Table(TableUser).WithContext(ctx).
		Find(user, "username = ?", value.Value).Error
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, errUserNotFound
		}
		return nil, fmt.Errorf("failed to get user: %w", err)
	}
	ret := &api.User{
		Id:       user.Id,
		Username: user.Username,
		Email:    user.Email,
		Password: user.Password,
	}
	g.sessions(ctx, ret)
	return ret, nil
}

func (g *GrpcService) sessions(ctx context.Context, user *api.User) {
	var sessions []*api.Session
	g.db.Table(TableSession).WithContext(ctx).Find(&sessions, "user_id = ?", user.Id)
	user.Sessions = sessions
}

func (g *GrpcService) Create(ctx context.Context, u *api.User) (*api.User, error) {
	return u, g.db.Table(TableUser).WithContext(ctx).Transaction(func(tx *gorm.DB) error {
		var exist int64
		// Check user exists
		err := tx.
			Select("id").
			Where("username = ?", u.Username).
			Count(&exist).Error
		if err != nil {
			return err
		}

		if exist != 0 {
			return common.ApiError{
				Code:    http.StatusBadRequest,
				Message: "username already exists",
			}
		}

		newId, err := common.NextID()
		if err != nil {
			return fmt.Errorf("failed to generate id: %w", err)
		}

		// Create user
		user := new(UserInfo)
		id := strconv.FormatUint(newId, 10)
		u.Id = id
		user.Id = id
		user.Username = u.Username
		user.Email = u.Email
		user.Password = u.Password
		return tx.Create(user).Error
	})
}

func (g *GrpcService) ResetPassword(ctx context.Context, u *api.User) (*emptypb.Empty, error) {
	var exist int64

	// Check user
	err := g.db.Table(TableUser).WithContext(ctx).
		Where("id = ?", u.Id).
		Count(&exist).Error
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, errUserNotFound
		}
		return nil, fmt.Errorf("failed to get user: %w", err)
	}

	return &emptypb.Empty{}, g.db.Table(TableUser).WithContext(ctx).
		Where("id = ?", u.Id).
		Update("password", u.Password).Error
}

func (g *GrpcService) SaveSession(ctx context.Context, action *api.SessionAction) (ret *emptypb.Empty, err error) {
	var exist int64
	db := g.db.WithContext(ctx)

	// Check user
	err = db.Table(TableUser).
		Where("id = ?", action.UserId).
		Count(&exist).Error
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return nil, errUserNotFound
		}
		return nil, fmt.Errorf("failed to get user: %w", err)
	}

	switch action.Action {
	case api.SessionAction_SignIn:
		// Create new session
		return ret, db.Table(TableSession).Create(&Session{
			Id:        action.Session.Id,
			UserId:    action.UserId,
			Ip:        action.Session.Ip,
			Client:    action.Session.Client,
			CreatedAt: action.Session.Timestamp,
			LastUsage: action.Session.Timestamp,
		}).Error
	case api.SessionAction_Refresh:
		// Update session last usage
		db.Table(TableSession).Update("lastUsage", action.Session.Timestamp)
	case api.SessionAction_Logout:
		// Delete session
		err = db.Table(TableSession).Delete(&api.Session{}, "id = ?", action.Session.Id).Error
		if err != nil {
			if errors.Is(err, gorm.ErrRecordNotFound) {
				return nil, common.ApiError{
					Code:    http.StatusNotFound,
					Message: "session not found",
				}
			}
			return nil, err
		}
	}

	return
}
