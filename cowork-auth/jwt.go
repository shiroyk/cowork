package main

import (
	"context"
	"errors"
	"fmt"
	"time"

	"github.com/golang-jwt/jwt/v5"
	"github.com/redis/go-redis/v9"
	common "github.com/shiroyk/cowork/common/golang"
)

const (
	prefixBlackListToken = "BLACKLIST_TOKEN_"
)

type tokenService struct {
	secret []byte
	redis  redis.UniversalClient
	parser *jwt.Parser
}

func newRedisClient(cfg config) (redis.UniversalClient, func(), error) {
	if common.IsDev() {
		opt, err := redis.ParseURL(common.DevRedis)
		if err != nil {
			return nil, nil, fmt.Errorf("failed to parse redis url: %w", err)
		}
		client := redis.NewClient(opt)
		return client, func() { client.Close() }, nil
	}

	opt, err := redis.ParseClusterURL(cfg.Redis)
	if err != nil {
		return nil, nil, fmt.Errorf("failed to parse redis url: %w", err)
	}
	client := redis.NewClusterClient(opt)
	return client, func() { client.Close() }, nil
}

func newTokenService(cfg config, redis redis.UniversalClient) *tokenService {
	return &tokenService{
		secret: []byte(cfg.JwtSecret),
		redis:  redis,
		parser: jwt.NewParser(jwt.WithIssuedAt(), jwt.WithValidMethods([]string{jwt.SigningMethodHS512.Alg()})),
	}
}

// ParseClaims returns the token Claims.
func (s *tokenService) ParseClaims(tokenString string) (ret jwt.RegisteredClaims, err error) {
	token, err := s.parser.ParseWithClaims(tokenString, &ret, func(token *jwt.Token) (any, error) {
		if _, ok := token.Method.(*jwt.SigningMethodHMAC); !ok {
			return nil, fmt.Errorf("unexpected signing method: %v", token.Header["alg"])
		}
		return s.secret, nil
	})
	if err != nil {
		return ret, err
	}
	return *token.Claims.(*jwt.RegisteredClaims), nil
}

// NewToken creates a new jwt token.
func (s *tokenService) NewToken(jti, issuer, subject string, expireAt time.Time) (string, error) {
	return jwt.NewWithClaims(jwt.SigningMethodHS512, jwt.RegisteredClaims{
		ID:        jti,
		Issuer:    issuer,
		Subject:   subject,
		IssuedAt:  jwt.NewNumericDate(time.Now()),
		ExpiresAt: jwt.NewNumericDate(expireAt),
	}).SignedString(s.secret)
}

// NewRefreshToken creates a new jwt refresh token.
func (s *tokenService) NewRefreshToken(jti, issuer, subject string, expireAt time.Time) (string, error) {
	return jwt.NewWithClaims(jwt.SigningMethodHS512, jwt.RegisteredClaims{
		ID:        jti,
		Issuer:    issuer,
		Subject:   subject,
		Audience:  []string{"refresh_token"},
		IssuedAt:  jwt.NewNumericDate(time.Now()),
		ExpiresAt: jwt.NewNumericDate(expireAt),
	}).SignedString(s.secret)
}

// IsTokenIdRevoked checks the token claims id is in blacklist.
func (s *tokenService) IsTokenIdRevoked(ctx context.Context, tokenId string) (bool, error) {
	i, err := s.redis.Exists(ctx, prefixBlackListToken+tokenId).Result()
	if err != nil {
		if errors.Is(err, redis.Nil) {
			return false, nil
		}
		return false, err
	}
	return i == 1, nil
}

// RevokeTokenId set token claims id is to blacklist.
func (s *tokenService) RevokeTokenId(ctx context.Context, tokenId string, expiration time.Duration) error {
	_, err := s.redis.Set(ctx, prefixBlackListToken+tokenId, true, expiration).Result()
	return err
}
