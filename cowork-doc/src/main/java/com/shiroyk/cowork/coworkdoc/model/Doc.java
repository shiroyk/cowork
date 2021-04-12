package com.shiroyk.cowork.coworkdoc.model;

import com.shiroyk.cowork.coworkcommon.constant.Permission;
import com.shiroyk.cowork.coworkcommon.dto.DocDto;
import com.shiroyk.cowork.coworkcommon.dto.GroupDto;
import com.shiroyk.cowork.coworkcommon.dto.UserDto;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Data
public class Doc {
    @Id
    private String id;
    private String title;
    private DocDto.DocUrl url;
    private DocDto.Owner owner;
    private boolean delete = false;
    private LocalDateTime createTime = LocalDateTime.now();
    private LocalDateTime updateTime = LocalDateTime.now();

    public Doc() {
    }

    public void setUpdateTime() {
        this.updateTime = LocalDateTime.now();
    }

    public String getOwnerId() {
        return this.owner != null ? this.owner.getId() : null;
    }

    public boolean hasPermission() {
        return this.url != null && this.url.hasPermission();
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
        this.url = new DocDto.DocUrl(UUID.randomUUID().toString().replace("-",""), permission);
    }

    public Permission getUrlPermission() {
        if (this.url == null)
            return Permission.Empty;
        return this.getUrl().getPermission();
    }
    public boolean belongUser() {
        return this.owner != null && this.owner.getOwner().equals(DocDto.OwnerEnum.User);
    }

    public boolean belongGroup() {
        return this.owner != null && this.owner.getOwner().equals(DocDto.OwnerEnum.Group);
    }
}
