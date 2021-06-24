package com.shiroyk.cowork.coworkcommon.model.doc;

import lombok.Data;

@Data
public class Owner {
    private String id;
    private OwnerEnum owner;

    public enum OwnerEnum {
        User, Group;
    }

    public Owner() {
    }

    public Owner(String id, OwnerEnum owner) {
        this.id = id;
        this.owner = owner;
    }
}
