package com.shiroyk.cowork.coworkcollab.model;

import lombok.Data;

import java.util.Set;

@Data
public class UserSet {
    private Action action;
    private String uid;
    private Set<String> users;

    public UserSet() {
    }

    public enum Action {
        login, logout;
    }

    public UserSet(Action action, String uid, Set<String> users) {
        this.action = action;
        this.uid = uid;
        this.users = users;
    }

    public static UserSet login(String uid, Set<String> users) {
        return new UserSet(Action.login, uid, users);
    }

    public static UserSet logout(String uid, Set<String> users) {
        return new UserSet(Action.logout, uid, users);
    }
}
