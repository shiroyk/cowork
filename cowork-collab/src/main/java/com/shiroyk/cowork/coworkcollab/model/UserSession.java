package com.shiroyk.cowork.coworkcollab.model;

import com.shiroyk.cowork.coworkcommon.constant.Permission;
import com.shiroyk.cowork.coworkcommon.constant.Role;
import lombok.Data;

import java.security.Principal;
import java.util.Objects;

@Data
public class UserSession implements Principal {
    private String name;
    private String did;
    private Role role;
    private Permission permission;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserSession that = (UserSession) o;
        return Objects.equals(name, that.name) && Objects.equals(did, that.did);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, did);
    }

    public UserSession(String name, Role role, Permission permission) {
        this.name = name;
        this.role = role;
        this.permission = permission;
    }

    public static UserSession create(String name, String role) {
        return new UserSession(name, Role.valueOf(role), Permission.Empty);
    }
}
