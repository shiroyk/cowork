package com.shiroyk.cowork.coworkadmin.dto;

import com.shiroyk.cowork.coworkcommon.dto.GroupDto;
import lombok.Value;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Value
public class UpdateGroup {
    @NotBlank(message = "群组名不能为空!")
    String name;
    @NotNull(message = "群组介绍不能为空!")
    String describe;
    @NotBlank(message = "群主不能为空!")
    String leader;
    @NotNull(message = "成员权限不能为空")
    GroupDto.MemberRole memberRole;
}
