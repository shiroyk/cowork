package common

import (
	"os"
	"strings"
	"sync"
)

var profileEnv = sync.OnceValue(func() string {
	profile := os.Getenv("PROFILE")
	if profile == "" {
		return "dev"
	}
	return profile
})

// IsK8s check is in k8s environment.
func IsK8s() bool { return strings.EqualFold(profileEnv(), "k8s") }

// IsDev check is in development environment.
func IsDev() bool { return !IsK8s() }

// ProfileValue return dev or k8s value.
func ProfileValue[T any](dev, k8s T) T {
	if IsDev() {
		return dev
	}
	return k8s
}

func Must(err error) {
	if err != nil {
		panic(err)
	}
}

func Must1[T any](ret T, err error) T {
	if err != nil {
		panic(err)
	}
	return ret
}

func Must2[T1 any, T2 any](t1 T1, t2 T2, err error) (T1, T2) {
	if err != nil {
		panic(err)
	}
	return t1, t2
}
