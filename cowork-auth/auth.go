package main

import (
	"errors"
	"net/http"
	"regexp"
	"strings"
	"time"

	"github.com/google/uuid"
	"github.com/labstack/echo/v4"
	common "github.com/shiroyk/cowork/common/src/main/golang"
	userapi "github.com/shiroyk/cowork/user/api/src/generated/golang/user/api"
	"github.com/shiroyk/cowork/user/api/src/main/golang/user/client"
	"golang.org/x/crypto/bcrypt"
	"google.golang.org/protobuf/types/known/wrapperspb"
)

const (
	refreshTokenExpireTime = time.Hour * 24 * 7
)

var (
	tokenExpireTime = common.ProfileValue(time.Hour*8, time.Minute*30)
	usernameRegex   = regexp.MustCompile("[a-zA-Z\\d_-]+")
	emailRegex      = regexp.MustCompile("^[a-zA-Z0-9_.]+@[a-z0-9]+\\.[a-z]+$")
)

func init() {
	router.POST("/api/sign_in", signIn)
	router.POST("/api/sign_up", signUp)
	router.POST("/api/refresh", refresh)
	router.POST("/api/logout", logout)
	router.POST("/api/logout/:id", logout)
	router.POST("/api/ping", ping)
	router.GET("/auth", auth)
	router.GET("/ping", func(c echo.Context) error { return c.NoContent(http.StatusOK) })
}

// validUser validates the User struct.
func validUser(user *userapi.User, checkEmail bool) error {
	if user == nil {
		return errors.New("user cannot be empty")
	}
	if !usernameRegex.Match([]byte(user.Username)) {
		return errors.New("username only contains letters, numbers, _- characters")
	}
	if len(user.Username) < 4 && len(user.Username) < 11 {
		return errors.New("username length must be above 4 and less than 10 characters")
	}
	if len(user.Password) < 8 && len(user.Password) < 17 {
		return errors.New("password length must be above 8 and less than 16 characters")
	}
	if checkEmail && !emailRegex.Match([]byte(user.Email)) {
		return errors.New("invalid email address")
	}
	return nil
}

// signUp saves the user and return the user id.
func signUp(c echo.Context) error {
	u := new(userapi.User)
	err := c.Bind(u)
	if err != nil {
		return err
	}
	if err = validUser(u, false); err != nil {
		return common.NewBadRequestApiError(err.Error())
	}

	password, err := bcrypt.GenerateFromPassword([]byte(u.GetPassword()), bcrypt.DefaultCost)
	if err != nil {
		return err
	}
	u.Password = string(password)

	u1, err := client.UserServiceClient().Create(common.RequestMetadata(c.Request()), u)
	if err != nil {
		return err
	}
	return c.JSON(http.StatusOK, map[string]any{
		"id": u1.Id,
	})
}

// signIn returns the jwt access_token and refresh_token.
func signIn(c echo.Context) (err error) {
	u := new(userapi.User)
	err = c.Bind(u)
	if err != nil {
		return err
	}
	if err = validUser(u, false); err != nil {
		return common.NewBadRequestApiError(err.Error())
	}

	var entity *userapi.User
	entity, err = client.UserServiceClient().FindByName(common.RequestMetadata(c.Request()), wrapperspb.String(u.GetUsername()))
	if err != nil {
		return err
	}
	err = bcrypt.CompareHashAndPassword([]byte(entity.Password), []byte(u.Password))
	if err != nil {
		return common.NewBadRequestApiError("invalid username or password")
	}

	clientId := c.Request().UserAgent() // TODO: unique client id
	now := time.Now()

	for _, session := range entity.GetSessions() {
		if session.GetClient() == clientId {
			if session.GetTimestamp() > now.Unix() {
				// revoke the token
				err = common.RevokeTokenId(c.Request().Context(), session.GetId(), time.Unix(session.GetTimestamp(), 0).Sub(now))
				if err != nil {
					return err
				}
			}

			// logout the session
			_, err = client.UserServiceClient().SaveSession(common.RequestMetadata(c.Request()), &userapi.SessionAction{
				Action: userapi.SessionAction_Logout,
				UserId: entity.GetId(),
				Session: &userapi.Session{
					Id:        session.GetId(),
					Ip:        c.RealIP(),
					Timestamp: now.Unix(),
				},
			})
			if err != nil {
				return err
			}
			break
		}
	}

	// create token and refresh token
	jti := uuid.New().String()
	token, err := common.NewToken(jti, entity.GetId(), entity.GetUsername(), now.Add(tokenExpireTime))
	if err != nil {
		return err
	}
	refreshToken, err := common.NewRefreshToken(jti, entity.GetId(), entity.GetUsername(), now.Add(refreshTokenExpireTime))
	if err != nil {
		return err
	}

	// sign in the session
	defer func() {
		if err != nil {
			return
		}
		_, err2 := client.UserServiceClient().SaveSession(common.RequestMetadata(c.Request()), &userapi.SessionAction{
			Action: userapi.SessionAction_SignIn,
			UserId: entity.GetId(),
			Session: &userapi.Session{
				Id:        jti,
				Client:    clientId,
				Ip:        c.RealIP(),
				Timestamp: now.Unix(),
			},
		})
		if err2 != nil {
			err = err2
		}
	}()

	return c.JSON(http.StatusOK, map[string]any{
		"expires_in":               tokenExpireTime.Seconds(),
		"access_token":             token,
		"refresh_token":            refreshToken,
		"refresh_token_expires_in": refreshTokenExpireTime.Seconds(),
		"token_type":               "bearer",
	})
}

// refresh the user token.
func refresh(c echo.Context) (err error) {
	token := c.Request().Header.Get("X-Refresh-Token")
	claims, err := common.ParseClaims(token)
	if err != nil {
		return &common.ApiError{Code: http.StatusUnauthorized, Message: err.Error()}
	}
	if len(claims.Audience) != 1 || claims.Audience[0] != "refresh_token" {
		return &common.ApiError{Code: http.StatusUnauthorized, Message: "invalid refresh token"}
	}

	revoked, err := common.IsTokenIdRevoked(c.Request().Context(), claims.ID)
	if err != nil {
		c.Logger().Errorf("error while check token claim revoke: %s", err)
		return common.NewApiError(http.StatusInternalServerError)
	}
	if revoked {
		return &common.ApiError{Code: http.StatusUnauthorized, Message: "refresh token expired"}
	}

	now := time.Now()
	var newToken, newRefreshToken string
	defer func() {
		if err != nil {
			return
		}

		_, err2 := client.UserServiceClient().SaveSession(common.RequestMetadata(c.Request()), &userapi.SessionAction{
			Action: userapi.SessionAction_Refresh,
			UserId: claims.Issuer,
			Session: &userapi.Session{
				Id:        claims.ID,
				Ip:        c.RealIP(),
				Timestamp: now.Unix(),
			},
		})
		if err2 != nil {
			err = err2
		}
	}()

	newToken, err = common.NewToken(claims.ID, claims.Issuer, claims.Subject, now.Add(tokenExpireTime))
	if err != nil {
		return err
	}

	// if the refresh token is about to expire, create a new refresh token
	if claims.ExpiresAt.Sub(now) < tokenExpireTime {
		newRefreshToken, err = common.NewRefreshToken(claims.ID, claims.Issuer, claims.Subject, now.Add(refreshTokenExpireTime))
		if err != nil {
			return err
		}
		return c.JSON(http.StatusOK, map[string]any{
			"expires_in":               tokenExpireTime.Seconds(),
			"access_token":             newToken,
			"refresh_token":            newRefreshToken,
			"refresh_token_expires_in": refreshTokenExpireTime.Seconds(),
		})
	}

	return c.JSON(http.StatusOK, map[string]any{
		"expires_in":   tokenExpireTime.Seconds(),
		"access_token": newToken,
	})
}

// logout the system.
func logout(c echo.Context) error {
	userId := c.Request().Header.Get("X-User-Id")
	sessionId := c.Param("id")
	var expiration time.Duration

	if sessionId != "" {
		// logout specific session
		entity, err := client.UserServiceClient().FindById(common.RequestMetadata(c.Request()), wrapperspb.String(userId))
		if err != nil {
			return err
		}
		for _, session := range entity.GetSessions() {
			if session.GetId() == sessionId {
				expiration = time.Unix(session.GetTimestamp(), 0).Add(refreshTokenExpireTime).Sub(time.Now())
				break
			}
		}
	} else {
		// logout current session
		token := strings.TrimPrefix(c.Request().Header.Get("Authorization"), "Bearer ")
		claims, err := common.ParseClaims(token)
		if err != nil {
			return err
		}
		sessionId = claims.ID
		expiration = claims.ExpiresAt.Sub(time.Now())
	}

	if expiration > 0 {
		// revoke the token
		if err := common.RevokeTokenId(c.Request().Context(), sessionId, expiration); err != nil {
			return err
		}
	}

	// logout the session
	_, err := client.UserServiceClient().SaveSession(common.RequestMetadata(c.Request()), &userapi.SessionAction{
		Action: userapi.SessionAction_Logout,
		UserId: userId,
		Session: &userapi.Session{
			Id:        sessionId,
			Ip:        c.RealIP(),
			Timestamp: time.Now().Unix(),
		},
	})
	if err != nil {
		return err
	}

	return c.NoContent(http.StatusNoContent)
}

// ping check the token.
func ping(c echo.Context) error {
	token := strings.TrimPrefix(c.Request().Header.Get("Authorization"), "Bearer ")
	claims, err := common.ParseClaims(token)
	if err != nil {
		return &common.ApiError{Code: http.StatusUnauthorized, Message: err.Error()}
	}

	if revoked, _ := common.IsTokenIdRevoked(c.Request().Context(), claims.ID); revoked {
		return &common.ApiError{Code: http.StatusUnauthorized, Message: "token expired"}
	}

	return c.NoContent(http.StatusNoContent)
}

// auth check the jwt token.
func auth(c echo.Context) error {
	token := strings.TrimPrefix(c.Request().Header.Get("Authorization"), "Bearer ")
	if token == "" {
		// browser websocket auth
		token = c.Request().Header.Get("Sec-WebSocket-Protocol")
	}
	claims, err := common.ParseClaims(token)
	if err != nil {
		return &common.ApiError{Code: http.StatusUnauthorized, Message: err.Error()}
	}

	revoked, err := common.IsTokenIdRevoked(c.Request().Context(), claims.ID)
	if err != nil {
		c.Logger().Errorf("error while check token claim revoke: %s", err)
		return common.NewApiError(http.StatusInternalServerError)
	}
	if revoked {
		return &common.ApiError{Code: http.StatusUnauthorized, Message: "token expired"}
	}

	c.Response().Header().Set("X-User-Id", claims.Issuer)

	return c.NoContent(http.StatusNoContent)
}
