package com.shiroyk.cowork.coworkcommon.model.doc;

import com.shiroyk.cowork.coworkcommon.constant.Permission;
import com.shiroyk.cowork.coworkcommon.dto.DocDto;
import com.shiroyk.cowork.coworkcommon.dto.GroupDto;
import com.shiroyk.cowork.coworkcommon.dto.UserDto;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Data
public class Doc {
    @Id
    private String id;
    private String title;
    private DocUrl url;
    private Owner owner;
    private boolean delete = false;
    @CreatedDate
    private LocalDateTime createTime;
    @LastModifiedDate
    private LocalDateTime updateTime;

    public Doc() {
    }

    public Doc(String title, Owner owner) {
        this.title = title;
        this.owner = owner;
    }

    public static Doc createUserDoc(String name, String uid) {
        return new Doc(name, new Owner(uid, Owner.OwnerEnum.User));
    }

    public static Doc createGroupDoc(String name, String gid) {
        return new Doc(name, new Owner(gid, Owner.OwnerEnum.Group));
    }

    public String getOwnerId() {
        return this.owner != null ? this.owner.getId() : null;
    }

    public boolean hasGetPermission() {
        return this.url != null && this.url.notEmptyPermission();
    }

    public boolean hasWritePermission() {
        return this.url != null && this.url.getPermission() != null && this.url.getPermission().equals(Permission.ReadWrite);
    }

    public DocDto toDocDto() {
        return new DocDto(id, url, title, delete, owner, createTime, updateTime);
    }

    public DocDto toDocDto(boolean putDoc) {
        return new DocDto(id, url, title, owner, putDoc, delete, createTime, updateTime);
    }

    public DocDto toDocDto(UserDto ownerDetail) {
        return new DocDto(id, url, title, owner, ownerDetail, createTime, updateTime);
    }

    public DocDto toDocDto(GroupDto ownerDetail) {
        return new DocDto(id, url, title, owner, ownerDetail, createTime, updateTime);
    }

    public void createUrl(Permission permission) {
        this.url = new DocUrl(new ObjectId().toString(), permission);
    }

    public Permission getUrlPermission() {
        if (this.url == null)
            return Permission.Empty;
        return this.getUrl().getPermission();
    }

    public boolean belongUser() {
        return Owner.OwnerEnum.User.equals(this.owner.getOwner());
    }

    public boolean belongGroup() {
        return Owner.OwnerEnum.Group.equals(this.owner.getOwner());
    }
}
