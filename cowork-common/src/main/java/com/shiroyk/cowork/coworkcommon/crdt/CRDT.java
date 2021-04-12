package com.shiroyk.cowork.coworkcommon.crdt;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
public class CRDT {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    Object insert;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    Integer delete;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    Integer format;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    AttributeMap attributes;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    Version version;

    public CRDT() {
    }

    public enum Type {
        format, insert, delete;
    }

    public Type type() {
        if (this.delete != null)
            return Type.delete;
        else if (this.format != null)
            return Type.format;
        else
            return Type.insert;
    }

    public boolean isInsert() {
        return type().equals(Type.insert);
    }

    public boolean isFormat() {
        return type().equals(Type.format);
    }

    public boolean isDelete() {
        return type().equals(Type.delete);
    }

    public int length() {
        if (this.delete != null)
            return this.delete;
        else if (this.format != null) {
            return this.format;
        } else {
            return this.insert instanceof String ? ((String) this.insert).length() : 1;
        }
    }
}