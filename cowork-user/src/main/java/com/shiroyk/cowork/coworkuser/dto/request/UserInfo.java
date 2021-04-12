package com.shiroyk.cowork.coworkuser.dto.request;

import lombok.Value;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Value
public class UserInfo {
    @NotBlank(message = "用户名不能为空！")
    @Size(max = 8, min = 2, message = "用户名必须在2-8字符之间！")
    String username;

    @NotBlank(message = "昵称不能为空！")
    @Size(max = 8, min = 2, message = "昵称必须在2-8字符之间！")
    String nickname;

    @NotBlank(message = "密码不能为空！")
    @Size(max = 16, min = 4, message = "密码必须在4-16字符之间！")
    String password;

    @NotBlank(message = "邮箱不能为空！")
    @Email
    String email;
}
