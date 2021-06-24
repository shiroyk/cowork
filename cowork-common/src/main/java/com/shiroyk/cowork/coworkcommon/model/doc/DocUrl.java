package com.shiroyk.cowork.coworkcommon.model.doc;

import com.shiroyk.cowork.coworkcommon.constant.Permission;
import lombok.Data;

@Data
public class DocUrl {
    private String url;
    private Permission permission;

    public DocUrl() {
    }

    public DocUrl(String url, Permission permission) {
        this.url = url;
        this.permission = permission;
    }

    public boolean notEmptyPermission() {
        return this.permission != null && !this.permission.equals(Permission.Empty);
    }
}
