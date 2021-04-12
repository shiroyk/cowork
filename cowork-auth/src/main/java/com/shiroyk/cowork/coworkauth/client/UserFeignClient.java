package com.shiroyk.cowork.coworkauth.client;

import com.shiroyk.cowork.coworkcommon.dto.APIResponse;
import com.shiroyk.cowork.coworkauth.model.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "cowork-user/client")
public interface UserFeignClient {

    @PostMapping("/info")
    APIResponse<User> getUserInfo(@RequestParam("name") String name);

    @PostMapping("/signup")
    APIResponse<Object> signup(@RequestParam("username") String username,
                               @RequestParam("email") String email,
                               @RequestParam("password") String password);

    @PostMapping("/reset")
    APIResponse<Object> reset(@RequestParam("email") String username,
                              @RequestParam("password") String password);
}
