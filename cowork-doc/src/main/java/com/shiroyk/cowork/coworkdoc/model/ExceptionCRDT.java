package com.shiroyk.cowork.coworkdoc.model;

import com.shiroyk.cowork.coworkcommon.crdt.CRDT;
import lombok.Data;

@Data
public class ExceptionCRDT {
    private String uid;
    private String did;
    private CRDT crdt;

    public ExceptionCRDT() {
    }

    public ExceptionCRDT(String did, CRDT crdt) {
        this.did = did;
        this.crdt = crdt;
    }

    public ExceptionCRDT(String uid, String did, CRDT crdt) {
        this.uid = uid;
        this.did = did;
        this.crdt = crdt;
    }

}
