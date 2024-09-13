package common

import (
	"os"
	"path/filepath"
	"strings"
)

// CreateDir creates the directory if not exists
func CreateDir(path string) (err error) {
	path = filepath.Dir(path)
	if !FileExists(path) {
		return os.MkdirAll(path, os.ModePerm)
	}
	return
}

// FileExists checks the file if it exists
func FileExists(path string) bool {
	if _, err := os.Stat(path); err != nil {
		return !os.IsNotExist(err)
	}
	return true
}

// ExpandPath expands path "." or "~"
func ExpandPath(path string) (string, error) {
	// expand local directory
	if strings.HasPrefix(path, ".") {
		cwd, err := os.Getwd()
		if err != nil {
			return "", err
		}
		return filepath.Join(cwd, path[1:]), nil
	}
	// expand ~ as shortcut for home directory
	if strings.HasPrefix(path, "~") {
		home, err := os.UserHomeDir()
		if err != nil {
			return "", err
		}
		return filepath.Join(home, path[1:]), nil
	}
	return path, nil
}
