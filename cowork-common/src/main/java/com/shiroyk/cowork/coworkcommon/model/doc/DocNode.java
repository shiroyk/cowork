package com.shiroyk.cowork.coworkcommon.model.doc;

import com.shiroyk.cowork.coworkcommon.crdt.AttributeMap;
import com.shiroyk.cowork.coworkcommon.crdt.Version;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

import java.util.Objects;

@Data
public class DocNode implements Comparable<DocNode> {
    @Id
    private String id;
    @Indexed
    private String docId;
    private Object content;
    private boolean tombstone = false;
    private AttributeMap attributes;
    @Indexed
    private Version version;
    @Indexed
    private Version preVersion;

    @Override
    public int compareTo(DocNode o) {
        int comp = this.preVersion.compareTo(o.preVersion);
        return comp == 0 ? this.version.compareTo(o.version) : -comp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocNode docNode = (DocNode) o;
        return  Objects.equals(docId, docNode.docId) &&
                Objects.equals(content, docNode.content) &&
                Objects.equals(version, docNode.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(docId, content, version);
    }

    public DocNode() {
    }

    public DocNode(String docId, Object content, AttributeMap attributes, Version version) {
        this.docId = docId;
        this.content = content;
        this.attributes = attributes;
        this.version = version;
    }

    public DocNode(String docId, Object content, AttributeMap attributes, Version version, Version preVersion) {
        this.docId = docId;
        this.content = content;
        this.attributes = attributes;
        this.version = version;
        this.preVersion = preVersion;
    }

    public int length() {
        if (this.content != null && this.tombstone) {
            return this.content instanceof String ? ((String) this.content).length() : 1;
        }
        return 0;
    }
}
