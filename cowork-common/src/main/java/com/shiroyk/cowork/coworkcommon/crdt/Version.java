package com.shiroyk.cowork.coworkcommon.crdt;

import lombok.Data;

import java.util.Objects;

@Data
public class Version implements Comparable<Version> {
    String uid;
    Integer version;

    public Version() {
    }

    public Version(String uid, Integer version) {
        this.uid = uid;
        this.version = version;
    }

    public static Version head() {
        return new Version("head", 0);
    }

    public Version increase() {
        Version nv = new Version(this.uid, this.version);
        nv.version++;
        return nv;
    }

    public Version copy(String uid) {
        return new Version(uid, this.version);
    }

    @Override
    public int compareTo(Version o) {
        if (this.version < o.version)
            return 1;
        else if (this.version > o.version)
            return -1;
        else
            return this.uid.compareTo(o.uid);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Version version1 = (Version) o;
        return Objects.equals(uid, version1.uid) && Objects.equals(version, version1.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uid, version);
    }
}
