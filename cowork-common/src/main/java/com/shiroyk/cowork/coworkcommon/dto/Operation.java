package com.shiroyk.cowork.coworkcommon.dto;

import com.shiroyk.cowork.coworkcommon.crdt.CRDT;
import lombok.Data;

@Data
public class Operation {
    private String uid;
    private String did;
    private CRDT[] crdts;

    public Operation() {
    }

    public Operation(Operation op) {
        this.uid = op.uid;
        this.did = op.did;
        this.crdts = op.crdts;
    }

    public Operation(String uid, String did, CRDT[] crdts) {
        this.uid = uid;
        this.did = did;
        this.crdts = crdts;
    }
}
