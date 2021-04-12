package com.shiroyk.cowork.coworkdoc.dto;

import com.shiroyk.cowork.coworkcommon.crdt.Version;
import com.shiroyk.cowork.coworkdoc.model.DocNode;
import lombok.Data;

import java.util.List;

@Data
public class DocContent {
    private Version version;
    private List<DocNode> content;

    public DocContent() {
    }

    public DocContent(Version version, List<DocNode> content) {
        this.version = version;
        this.content = content;
    }
}
