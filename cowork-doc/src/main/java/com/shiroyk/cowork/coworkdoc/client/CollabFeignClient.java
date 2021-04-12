package com.shiroyk.cowork.coworkdoc.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(value = "cowork-collab/collab")
public interface CollabFeignClient {
    @GetMapping("/onlineUser")
    Long getOnlineUser();
}
