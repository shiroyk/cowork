package com.shiroyk.cowork.coworkcommon.constant;

public enum Role {
    Normal(0b00),
    Admin(0b11);

    private final Integer role;

    Role(int role) {
        this.role = role;
    }

    public Integer getRole() {
        return role;
    }

    public static Role valueOf(Integer role) {
        if (role == 0b11) {
            return Admin;
        }
        return Normal;
    }
}
