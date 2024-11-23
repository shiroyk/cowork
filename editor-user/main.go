package main

import common "github.com/shiroyk/crdt-editor/common/golang"

func main() {
	start := common.Must1(initialize())
	start()
}
