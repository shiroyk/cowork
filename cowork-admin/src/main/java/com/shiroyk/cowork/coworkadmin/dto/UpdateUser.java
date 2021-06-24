package com.shiroyk.cowork.coworkadmin.dto;

import lombok.Value;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Value
public class UpdateUser {
    @NotBlank(message = "用户名不能为空!")
    String username;
    @NotBlank(message = "用户昵称不能为空!")
    String nickname;
    @NotBlank(message = "用户密码不能为空!")
    String password;
    @Email(message = "邮箱格式不正确!")
    String email;
    @NotBlank(message = "用户权限不能为空!")
    String role;
}
