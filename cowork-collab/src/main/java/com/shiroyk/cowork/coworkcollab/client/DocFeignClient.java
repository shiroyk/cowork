package com.shiroyk.cowork.coworkcollab.client;

import com.shiroyk.cowork.coworkcommon.constant.Permission;
import com.shiroyk.cowork.coworkcommon.dto.APIResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "cowork-doc/client")
public interface DocFeignClient {
    @PostMapping("/permission")
    APIResponse<Permission> getPermission(@RequestParam String uid, @RequestParam String did);
}
