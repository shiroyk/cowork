package main

import common "github.com/shiroyk/cowork/common/golang"

func main() {
	start, clean := common.Must2(initialize())
	defer clean()
	start()
}
