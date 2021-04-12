package com.shiroyk.cowork.coworkauth.controller;

import com.shiroyk.cowork.coworkcommon.dto.APIResponse;
import com.shiroyk.cowork.coworkauth.dto.request.NewUser;
import com.shiroyk.cowork.coworkauth.dto.request.ResetPass;
import com.shiroyk.cowork.coworkauth.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/oauth")
@AllArgsConstructor
public class AuthController {
    private final UserService userService;

    /**
     * @Description: 用户注册
     * @param user
     */
    @PostMapping("/signup")
    public APIResponse<Object> signup(@Valid NewUser user) {
        return userService.signup(user);
    }

    /**
     * @Description: 用户重置密码
     * @param reset
     */
    @PostMapping("/reset")
    public APIResponse<Object> resetPassword(@Valid ResetPass reset) {
        return userService.reset(reset);
    }
}
