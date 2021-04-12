package com.shiroyk.cowork.coworkcollab.model;

import lombok.Data;

@Data
public class EditorCursor {
    private String uid;
    private int index;
    private int length;

    public EditorCursor() {
    }

    public EditorCursor(String uid, int index, int length) {
        this.uid = uid;
        this.index = index;
        this.length = length;
    }
}
