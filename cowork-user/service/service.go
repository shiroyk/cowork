package service

import (
	"context"
	"fmt"
	"strconv"

	common "github.com/shiroyk/cowork/common/golang"
	. "github.com/shiroyk/cowork/user/model"
	"gorm.io/gorm"
)

type UserService interface {
	Search(ctx context.Context, query UserQuery) ([]User, int64, error)
	Save(ctx context.Context, u User) error
	Get(ctx context.Context, id uint64) (User, error)
}

type serviceImpl struct {
	db *gorm.DB
}

func NewUserService(db *gorm.DB) UserService {
	return &serviceImpl{db}
}

func (s *serviceImpl) Search(ctx context.Context, query UserQuery) (ret []User, size int64, err error) {
	err = s.db.Table(TableUser).WithContext(ctx).
		Scopes(query.Where).
		Find(&ret).
		Count(&size).Error
	return
}

func (s *serviceImpl) Save(ctx context.Context, u User) error {
	return s.db.Transaction(func(tx *gorm.DB) error {
		var exist string

		err := s.db.Select("id").
			Where("username = ?", u.Username).
			Find(&exist).Error
		if err != nil {
			return err
		}

		// Exists user
		uid, ok := common.UserIdFromContext(ctx)
		if ok && exist != "" {
			if exist != uid {
				return fmt.Errorf("username %s already exists", u.Username)
			}
			return s.db.Table(TableUser).WithContext(ctx).
				Where("id = ?", u.Id).
				Updates(&u).Error
		}

		// Create new user
		newId, err := common.NextID()
		if err != nil {
			return fmt.Errorf("failed to generate id: %w", err)
		}
		u.Id = strconv.FormatUint(newId, 10)
		return s.db.Table(TableUser).WithContext(ctx).
			Create(&u).Error
	})
}

func (s *serviceImpl) Get(ctx context.Context, id uint64) (User, error) {
	var ret User
	err := s.db.Table(TableUser).WithContext(ctx).
		Where("id = ?", id).
		First(&ret).Error
	return ret, err
}
