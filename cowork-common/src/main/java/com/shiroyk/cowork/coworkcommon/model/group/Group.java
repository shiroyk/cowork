package com.shiroyk.cowork.coworkcommon.model.group;

import com.shiroyk.cowork.coworkcommon.dto.GroupDto;
import com.shiroyk.cowork.coworkcommon.dto.UserDto;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
public class Group {
    @Id
    private String id;
    private String name;
    private String describe;
    private String leader;
    private Set<String> users = new HashSet<>();
    private Set<String> docs = new HashSet<>();
    private Set<String> apply = new HashSet<>();
    private Boolean isEnable = true;
    private GroupDto.MemberRole memRole = GroupDto.MemberRole.Create;
    @CreatedDate
    private LocalDateTime createTime;
    @LastModifiedDate
    private LocalDateTime updateTime;

    public Group() {
    }

    public Group(String id, String name, HashSet<String> users) {
        this.id = id;
        this.name = name;
        this.users = users;
    }

    public GroupDto toGroupDto() {
        return new GroupDto(id, name, describe, leader, memRole);
    }

    public GroupDto toGroupDtoM(UserDto userDto, Integer user, Integer doc) {
        return new GroupDto(id, name, describe, leader, userDto, user, doc, memRole);
    }

    public void setIsEnable() {
        isEnable = !isEnable;
    }
}
