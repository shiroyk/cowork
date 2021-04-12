package com.shiroyk.cowork.coworkgroup.model;

import com.shiroyk.cowork.coworkcommon.dto.GroupDto;
import lombok.Data;
import org.springframework.data.annotation.Id;

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
    private LocalDateTime createTime = LocalDateTime.now();
    private LocalDateTime updateTime = LocalDateTime.now();

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

    public void setIsEnable() {
        isEnable = !isEnable;
    }

    public void setUpdateTime() {
        this.updateTime = LocalDateTime.now();
    }
}
