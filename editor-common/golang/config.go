package common

import (
	"fmt"
	"log/slog"
	"os"
	"path/filepath"
	"sync/atomic"

	"github.com/fsnotify/fsnotify"
	"gopkg.in/yaml.v3"
)

const configFile = "/etc/app/config.yaml"

// ReadYaml read the YAML file and convert it to T
func ReadYaml[T any](path string) (T, error) {
	var cfg T
	path, err := ExpandPath(path)
	if err != nil {
		return cfg, err
	}
	data, err := os.ReadFile(configFile)
	if err != nil {
		return cfg, fmt.Errorf("failed to read configuration file: %s", err)
	}

	expand := os.ExpandEnv(string(data))
	if err = yaml.Unmarshal([]byte(expand), &cfg); err != nil {
		return cfg, fmt.Errorf("failed to unmarshal configuration: %s", err)
	}
	return cfg, nil
}

// Config read the YAML file config
func Config[T any](devConfig T) T {
	if IsDev() {
		return devConfig
	}
	cfg, err := ReadYaml[T](configFile)
	if err != nil {
		panic(err)
	}
	return cfg
}

// WatchConfig read the YAML file config.
// The config will be reloaded on file changes.
func WatchConfig[T any](devConfig T) func() T {
	if IsDev() {
		return func() T { return devConfig }
	}

	var config atomic.Pointer[T]

	cfg, err := ReadYaml[T](configFile)
	if err != nil {
		panic(err)
	}
	config.Store(&cfg)

	// Create a file watcher to reload configuration on changes
	watcher, err := fsnotify.NewWatcher()
	if err != nil {
		slog.Error("failed to create file watcher", slog.String("error", err.Error()))
	}

	realConfigFile, _ := filepath.EvalSymlinks(configFile)

	go func() {
		defer watcher.Close()

		configPath, _ := filepath.Split(configFile)
		if err := watcher.Add(configPath); err != nil {
			slog.Error("failed to add file watcher", slog.String("error", err.Error()))
			return
		}

		for {
			select {
			case event, ok := <-watcher.Events:
				if !ok {
					return
				}
				// Reload the config on event
				currentConfigFile, _ := filepath.EvalSymlinks(configFile)
				// we only care about the config file with the following cases:
				// 1 - if the config file was modified or created
				// 2 - if the real path to the config file changed (eg: k8s ConfigMap replacement)
				if (filepath.Clean(event.Name) == configFile &&
					(event.Has(fsnotify.Write) || event.Has(fsnotify.Create))) ||
					(currentConfigFile != "" && currentConfigFile != realConfigFile) {
					realConfigFile = currentConfigFile
					cfg, err := ReadYaml[T](configFile)
					if err != nil {
						slog.Error("failed to reload configuration", slog.String("error", err.Error()))
					} else {
						config.Store(&cfg)
					}
				} else if filepath.Clean(event.Name) == configFile && event.Has(fsnotify.Remove) {
					return
				}
			case err, ok := <-watcher.Errors:
				if !ok {
					return
				}
				slog.Error("file watcher error", slog.String("error", err.Error()))
			}
		}
	}()

	return func() T { return *config.Load() }
}
