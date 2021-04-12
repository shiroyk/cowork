package com.shiroyk.cowork.coworkcommon.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.shiroyk.cowork.coworkcommon.constant.Role;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class UserDto {
    private String id;
    private String username;
    private String nickname;
    private String email;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String avatar;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String group;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Role role;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<String> recent;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Set<String> star;


    public UserDto() {
    }

    public UserDto(String id, String username, String nickname, String email) {
        this.id = id;
        this.username = username;
        this.nickname = nickname;
        this.email = email;
    }

    public UserDto(String id, String username, String nickname, String email, String avatar) {
        this.id = id;
        this.username = username;
        this.nickname = nickname;
        this.email = email;
        this.avatar = avatar;
    }

    public UserDto(String id, String username, String nickname, String email, String group, String avatar) {
        this.id = id;
        this.username = username;
        this.nickname = nickname;
        this.email = email;
        this.group = group;
        this.avatar = avatar;
    }

    public UserDto(String id, String username, String nickname, String email, String group, String avatar, List<String> recent, Set<String> star, Role role) {
        this.id = id;
        this.username = username;
        this.nickname = nickname;
        this.email = email;
        this.group = group;
        this.role = role;
        this.recent = recent;
        this.avatar = avatar;
        this.star = star;
    }
}
