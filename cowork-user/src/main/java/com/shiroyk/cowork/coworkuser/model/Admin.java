package com.shiroyk.cowork.coworkuser.model;

import com.shiroyk.cowork.coworkcommon.constant.Role;
import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public class Admin {
    @Id
    private String id;
    private String username;
    private String nickname;
    private String email;
    private String password;
    private String avatar;
    private Role role;
}
