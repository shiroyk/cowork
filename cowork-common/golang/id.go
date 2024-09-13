package common

import (
	"fmt"
	"hash/fnv"
	"os"
	"sync"
	"time"

	"github.com/sony/sonyflake"
)

var snow = sync.OnceValue(func() *sonyflake.Sonyflake {
	return Must1(sonyflake.New(sonyflake.Settings{
		StartTime: time.Date(2024, 1, 1, 0, 0, 0, 0, time.UTC),
		MachineID: func() (uint16, error) {
			// hash the pod name
			h := fnv.New32a()
			name := os.Getenv("HOSTNAME")
			_, err := h.Write([]byte(name))
			if err != nil {
				return 0, fmt.Errorf("failed to hash pod name: %s, %w", name, err)
			}
			defer h.Reset()
			return uint16(h.Sum32() & 0xFFFF), nil
		},
	}))
})

func NextID() (uint64, error) {
	return snow().NextID()
}
