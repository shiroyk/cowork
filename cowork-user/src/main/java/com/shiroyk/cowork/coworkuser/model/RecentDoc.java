package com.shiroyk.cowork.coworkuser.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Objects;

@Data
public class RecentDoc implements Comparable<RecentDoc> {
    private String docId;
    private LocalDateTime accessTime;

    public RecentDoc(String docId) {
        this.docId = docId;
        this.accessTime = LocalDateTime.now();
    }

    @Override
    public int compareTo(RecentDoc o) {
        return accessTime.compareTo(o.accessTime);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RecentDoc recentDoc = (RecentDoc) o;
        return Objects.equals(docId, recentDoc.docId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(docId);
    }
}
