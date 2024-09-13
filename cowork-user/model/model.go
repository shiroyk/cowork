package model

import (
	"database/sql"

	common "github.com/shiroyk/cowork/common/golang"
	"gorm.io/gorm"
)

const (
	TableUser    = "cowork_user"
	TableSession = "sessions"
)

type UserQuery struct {
	common.BaseQuery
	Ids      []uint64 `form:"ids"`
	Username *string  `form:"username"`
	Email    *string  `form:"email"`
	Nickname *string  `form:"nickname"`
}

func (q UserQuery) Where(db *gorm.DB) *gorm.DB {
	if q.Ids != nil {
		db = db.Where("id in ?", q.Ids)
	}
	if q.Username != nil {
		db = db.Where("username = ?", *q.Username)
	}
	if q.Email != nil {
		db = db.Where("email = ?", *q.Email)
	}
	if q.Nickname != nil {
		db = db.Where("nickname = ?", *q.Nickname)
	}
	return db.Limit(q.GetLimit()).Offset(q.Offset)
}

type User struct {
	Id       string           `json:"id"`
	Username string           `json:"username" gorm:"unique;size:16"`
	Nickname string           `json:"nickname" gorm:"size:16"`
	Email    string           `json:"email" gorm:"unique;size:128"`
	Avatar   sql.Null[string] `json:"avatar"`
}

func (User) TableName() string {
	return TableUser
}

type UserInfo struct {
	User      `gorm:"embedded"`
	Password  string `gorm:"size:128"`
	Enable    bool   `gorm:"default:true"`
	CreatedAt int64  `gorm:"autoUpdateTime:milli"`
	UpdatedAt int64  `gorm:"autoUpdateTime:milli"`
}

func (UserInfo) TableName() string { return TableUser }

type Session struct {
	Id        string `json:"id" gorm:"primaryKey"`
	UserId    string `json:"userId" gorm:"index"`
	Ip        string `json:"ip"`
	Client    string `json:"client"`
	CreatedAt int64  `json:"createdAt"`
	LastUsage int64  `json:"lastUsage"`
}

func (Session) TableName() string { return TableSession }

type Sessions []Session

func (s Sessions) Len() int {
	return len(s)
}

func (s Sessions) Less(i, j int) bool {
	return s[i].LastUsage > s[j].LastUsage
}

func (s Sessions) Swap(i, j int) {
	s[i], s[j] = s[j], s[i]
}
