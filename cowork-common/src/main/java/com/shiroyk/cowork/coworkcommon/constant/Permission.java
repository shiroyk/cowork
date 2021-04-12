package com.shiroyk.cowork.coworkcommon.constant;

public enum Permission {
    Empty(0),
    ReadOnly(1),
    ReadWrite(2);

    private final int per;

    Permission(int per) {
        this.per = per;
    }

    public int getPer() {
        return per;
    }

}
