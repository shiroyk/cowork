package common

import (
	"bytes"
	"context"
	"fmt"
	"io"
	"log/slog"
	"os"
	"runtime"
	"strconv"
	"sync"
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
	return slog.New(NewConsoleHandler(LoggerWriter(), LogLevel()))
}

var bufPool = sync.Pool{
	New: func() any {
		return new(bytes.Buffer)
	},
}

func freeBuffer(buf *bytes.Buffer) {
	buf.Reset()
	bufPool.Put(buf)
}

// ConsoleHandler is a Handler that writes Records to an io.Writer.
type ConsoleHandler struct {
	level        slog.Leveler
	w            io.Writer
	attrs, group string
	noColor      bool
}

// NewConsoleHandler creates a ConsoleHandler that writes to w,
// using the default options.
func NewConsoleHandler(w io.Writer, l slog.Leveler) *ConsoleHandler {
	return &ConsoleHandler{
		level:   l,
		w:       w,
		noColor: os.Getenv("NO_COLOR") != "",
	}
}

// Enabled reports whether the handler handles records at the given level.
// The handler ignores records whose level is lower.
func (c *ConsoleHandler) Enabled(_ context.Context, l slog.Level) bool {
	minLevel := slog.LevelInfo
	if c.level != nil {
		minLevel = c.level.Level()
	}
	return l >= minLevel
}

// WithAttrs With returns a new ConsoleHandler whose attributes consists
// of h's attributes followed by attrs.
func (c *ConsoleHandler) WithAttrs(attrs []slog.Attr) slog.Handler {
	buf := bufPool.Get().(*bytes.Buffer)
	defer freeBuffer(buf)

	for _, attr := range attrs {
		buf.WriteString(attr.String())
	}

	return &ConsoleHandler{
		level:   c.level,
		w:       c.w,
		group:   c.group,
		attrs:   buf.String(),
		noColor: c.noColor,
	}
}

// WithGroup returns a new Handler with the given group appended to
// the receiver's existing groups.
func (c *ConsoleHandler) WithGroup(name string) slog.Handler {
	return &ConsoleHandler{
		level:   c.level,
		w:       c.w,
		group:   name,
		attrs:   c.attrs,
		noColor: c.noColor,
	}
}

// Handle formats its argument Record as single line.
//
// If the Record's time is zero, the time is omitted.
//
// If the Record's level is zero, the level is omitted.
// Otherwise, the key is "level"
// and the value of [Level.String] is output.
//
// Each call to Handle results in a single serialized call to io.Writer.Write.
func (c *ConsoleHandler) Handle(_ context.Context, r slog.Record) (err error) {
	time := ""
	if !r.Time.IsZero() {
		time = r.Time.Format("2006-01-02 15:04:05.000")
	}

	buf := bufPool.Get().(*bytes.Buffer)
	defer freeBuffer(buf)

	fs := runtime.CallersFrames([]uintptr{r.PC})
	f, _ := fs.Next()
	buf.WriteString(f.Function)
	buf.WriteString(" - ")
	buf.WriteString(strconv.Itoa(f.Line))

	buf.WriteByte(' ')
	buf.WriteString(": ")
	buf.WriteString(r.Message)
	buf.WriteByte(' ')

	r.Attrs(func(a slog.Attr) bool {
		buf.WriteString(a.Key)
		buf.WriteString(": ")
		buf.WriteString(a.Value.String())
		buf.WriteByte(' ')
		return true
	})

	levelColor := grey
	switch r.Level {
	case slog.LevelDebug:
		levelColor = blue
	case slog.LevelWarn:
		levelColor = yellow
	case slog.LevelError:
		levelColor = red
	}

	if c.noColor {
		_, err = fmt.Fprintf(c.w, "%s %s %s\n", time, r.Level.String(), buf.String())
		return
	}

	_, err = fmt.Fprintf(c.w, "%s \x1b[%dm%s \x1b[0m%s\n", time, levelColor, r.Level.String(), buf.String())

	return
}
