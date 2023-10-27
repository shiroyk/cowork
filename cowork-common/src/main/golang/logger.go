package common

import (
	"io"
	"log/slog"
	"os"
)

var logFile = "/var/log/app/app.log"

func LogLevel() slog.Level {
	if IsDev() {
		return slog.LevelDebug
	}
	str := os.Getenv("LOG_LEVEL")
	if len(str) == 0 {
		return slog.LevelError
	}
	var level slog.Level
	err := level.UnmarshalText([]byte(str))
	if err != nil {
		return slog.LevelError
	}
	return level
}

func LoggerWriter() io.Writer {
	var writer io.Writer = os.Stdout
	if !IsDev() {
		if err := CreateDir(logFile); err != nil {
			panic(err)
		}
		logWriter, err := os.OpenFile(logFile, os.O_APPEND|os.O_CREATE|os.O_WRONLY, 0644)
		if err != nil {
			panic(err)
		}
		writer = io.MultiWriter(os.Stdout, logWriter)
	}
	return writer
}

func NewLogger() *slog.Logger {
	return slog.New(slog.NewTextHandler(LoggerWriter(), &slog.HandlerOptions{Level: LogLevel()}))
}
