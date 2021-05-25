package com.shiroyk.cowork.coworkcommon.dto;

import com.shiroyk.cowork.coworkcommon.crdt.CRDT;
import lombok.Data;

@Data
public class UploadDoc {
    private String docName;
    private CRDT[] crdts;

    public UploadDoc() {
    }
}
