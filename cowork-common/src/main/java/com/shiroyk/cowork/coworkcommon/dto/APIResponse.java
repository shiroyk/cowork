package com.shiroyk.cowork.coworkcommon.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.shiroyk.cowork.coworkcommon.constant.ResultCode;
import lombok.Data;

import java.io.Serializable;
import java.util.StringJoiner;

@Data
public class APIResponse<T> implements Serializable {
    private ResultCode code;
    private String msg;
    @JsonInclude(Include.NON_NULL)
    private T data;

    public APIResponse() {
    }

    public APIResponse(ResultCode code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public APIResponse(ResultCode code, String msg) {
        this(code, msg, null);
    }

    public static <T> APIResponse<T> ok() {
        return new APIResponse<>(ResultCode.Ok, "Ok");
    }

    public static <T> APIResponse<T> ok(String msg) {
        return new APIResponse<>(ResultCode.Ok, msg);
    }

    public static <T> APIResponse<T> ok(T data) {
        return new APIResponse<>(ResultCode.Ok, "Ok", data);
    }

    public static <T> APIResponse<T> ok(String msg, T data) {
        return new APIResponse<>(ResultCode.Ok, msg, data);
    }

    public static <T> APIResponse<T> badRequest(String msg) {
        return new APIResponse<>(ResultCode.BadRequest, msg);
    }

    public static <T> APIResponse<T> create(ResultCode code, String msg) {
        return new APIResponse<>(code, msg);
    }

    public static <T> APIResponse<T> create(ResultCode code, String msg, T data) {
        return new APIResponse<>(code, msg, data);
    }

    public String toJsonStr() {
        return new StringJoiner(", ", "{", "}")
                .add("\"code\":" + code.getCode())
                .add("\"msg\":\"" + msg + "\"")
                .toString();
    }
}
