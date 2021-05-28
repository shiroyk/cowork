package com.shiroyk.cowork.coworkdoc.dto;

import com.shiroyk.cowork.coworkcommon.crdt.Version;
import com.shiroyk.cowork.coworkdoc.model.DocNode;
import lombok.Data;

import java.util.List;

@Data
public class DocContent {
    private boolean readOnly;
    private Version version;
    private List<DocNode> content;

    public DocContent() {
    }

    public DocContent(boolean readOnly, Version version, List<DocNode> content) {
        this.readOnly = readOnly;
        this.version = version;
        this.content = content;
    }
}
