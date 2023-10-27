package common

import (
	"context"
	"errors"
	"fmt"
	"os"
	"sync"
	"time"

	"github.com/golang-jwt/jwt/v5"
	"github.com/redis/go-redis/v9"
)

var jwtSecret = sync.OnceValue(func() []byte {
	secret := os.Getenv("JWT_SECRET_KEY")
	if secret == "" {
		secret = "COWORK"
	}
	return []byte(secret)
})

var jwtParser = sync.OnceValue(func() *jwt.Parser {
	return jwt.NewParser(jwt.WithIssuedAt(), jwt.WithValidMethods([]string{jwt.SigningMethodHS512.Alg()}))
})

// ParseClaims returns the token Claims.
func ParseClaims(tokenString string) (ret jwt.RegisteredClaims, err error) {
	token, err := jwtParser().ParseWithClaims(tokenString, &ret, func(token *jwt.Token) (any, error) {
		if _, ok := token.Method.(*jwt.SigningMethodHMAC); !ok {
			return nil, fmt.Errorf("unexpected signing method: %v", token.Header["alg"])
		}
		return jwtSecret(), nil
	})
	if err != nil {
		return ret, err
	}
	return *token.Claims.(*jwt.RegisteredClaims), nil
}

// NewToken creates a new jwt token.
func NewToken(jti, issuer, subject string, expireAt time.Time) (string, error) {
	return jwt.NewWithClaims(jwt.SigningMethodHS512, jwt.RegisteredClaims{
		ID:        jti,
		Issuer:    issuer,
		Subject:   subject,
		IssuedAt:  jwt.NewNumericDate(time.Now()),
		ExpiresAt: jwt.NewNumericDate(expireAt),
	}).SignedString(jwtSecret())
}

// NewRefreshToken creates a new jwt refresh token.
func NewRefreshToken(jti, issuer, subject string, expireAt time.Time) (string, error) {
	return jwt.NewWithClaims(jwt.SigningMethodHS512, jwt.RegisteredClaims{
		ID:        jti,
		Issuer:    issuer,
		Subject:   subject,
		Audience:  []string{"refresh_token"},
		IssuedAt:  jwt.NewNumericDate(time.Now()),
		ExpiresAt: jwt.NewNumericDate(expireAt),
	}).SignedString(jwtSecret())
}

// IsTokenIdRevoked checks the token claims id is in blacklist.
func IsTokenIdRevoked(ctx context.Context, tokenId string) (bool, error) {
	i, err := RedisClient().Exists(ctx, PrefixBlackListToken+tokenId).Result()
	if err != nil {
		if errors.Is(err, redis.Nil) {
			return false, nil
		}
		return false, err
	}
	return i == 1, nil
}

// RevokeTokenId set token claims id is to blacklist.
func RevokeTokenId(ctx context.Context, tokenId string, expiration time.Duration) error {
	_, err := RedisClient().Set(ctx, PrefixBlackListToken+tokenId, true, expiration).Result()
	return err
}
