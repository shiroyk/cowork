package com.shiroyk.cowork.coworkcommon.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@Data
public class GroupDto {
    private String id;
    private String name;
    private String describe;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String leader_id;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private UserDto leader;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer user;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer doc;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<UserDto> users;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<DocDto> docs;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private MemberRole memberRole;

    public enum MemberRole {
        ReadWrite, Create, CreateDelete;
    }

    public GroupDto() {
    }

    public GroupDto(String id, String name, String describe, String leader_id, MemberRole memberRole) {
        this.id = id;
        this.name = name;
        this.describe = describe;
        this.leader_id = leader_id;
        this.memberRole = memberRole;
    }

    public GroupDto(String id, String name, String describe, UserDto leader, Integer user, MemberRole memberRole) {
        this.id = id;
        this.name = name;
        this.describe = describe;
        this.leader = leader;
        this.user = user;
        this.memberRole = memberRole;
    }

    public GroupDto(String id, String name, String describe, UserDto leader, Integer user, Integer doc, MemberRole memberRole) {
        this.id = id;
        this.name = name;
        this.describe = describe;
        this.leader = leader;
        this.user = user;
        this.doc = doc;
        this.memberRole = memberRole;
    }
}
