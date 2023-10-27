package common

import "fmt"

const (
	red    = 31
	green  = 32
	yellow = 33
	blue   = 36
	grey   = 38
)

func color(i int, str string) string { return fmt.Sprintf("\x1b[%dm%s\x1b[0m", i, str) }

// Red returns string with red color.
func Red(str string) string { return color(red, str) }

// Green returns string with green color.
func Green(str string) string { return color(green, str) }

// Yellow returns string with yellow color.
func Yellow(str string) string { return color(yellow, str) }

// Blue returns string with blue color.
func Blue(str string) string { return color(blue, str) }

// Grey returns string with grey color.
func Grey(str string) string { return color(grey, str) }
