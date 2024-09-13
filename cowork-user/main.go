package main

import common "github.com/shiroyk/cowork/common/golang"

func main() {
	start := common.Must1(initialize())
	start()
}
