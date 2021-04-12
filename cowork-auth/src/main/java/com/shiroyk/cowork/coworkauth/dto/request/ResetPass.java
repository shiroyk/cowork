package com.shiroyk.cowork.coworkauth.dto.request;

import lombok.Value;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Value
public class ResetPass {
    @NotBlank(message = "用户名不能为空！")
    String username;
    @NotBlank(message = "验证码不能为空！")
    String code;
    @NotBlank(message = "密码不能为空！")
    @Size(max = 16, min = 4, message = "密码必须在4-16字符之间！")
    String password;
}
