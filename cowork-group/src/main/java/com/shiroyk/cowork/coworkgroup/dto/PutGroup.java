package com.shiroyk.cowork.coworkgroup.dto;

import com.shiroyk.cowork.coworkcommon.dto.GroupDto;
import lombok.Value;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Value
public class PutGroup {
    @NotBlank(message = "群组名不能为空！")
    @Size(max = 8, min = 2, message = "群组名必须在2-8字符之间！")
    String name;

    @NotBlank(message = "群组信息不能为空！")
    @Size(max = 200, message = "群组信息不能超过200字符！")
    String describe;

    GroupDto.MemberRole memberRole;
}
