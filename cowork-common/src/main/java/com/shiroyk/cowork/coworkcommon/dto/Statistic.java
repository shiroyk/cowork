package com.shiroyk.cowork.coworkcommon.dto;

import lombok.Data;

@Data
public class Statistic {
    private Long collab;
    private Long user;
    private Long group;
    private Long doc;

    public Statistic() {
    }

    public Statistic(Long collab, Long user, Long group, Long doc) {
        this.collab = collab;
        this.user = user;
        this.group = group;
        this.doc = doc;
    }
}
