package com.shiroyk.cowork.coworkauth.dto;

import lombok.Data;

@Data
public class OAuthErrorCode {
    String code;

    public OAuthErrorCode(String code) {
        this.code = code;
    }
}
