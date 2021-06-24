package com.shiroyk.cowork.coworkadmin.dto;

import com.shiroyk.cowork.coworkcommon.constant.Permission;
import lombok.Value;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Value
public class UpdateDoc {
    @NotBlank(message = "文档名不能为空!")
    String title;
    @NotBlank(message = "用户Id不能为空!")
    String owner;
    @NotNull(message = "权限不能为空!")
    Permission permission;
    boolean isDelete;
}
