package main

import common "github.com/shiroyk/crdt-editor/common/golang"

func main() {
	start, clean := common.Must2(initialize())
	defer clean()
	start()
}
