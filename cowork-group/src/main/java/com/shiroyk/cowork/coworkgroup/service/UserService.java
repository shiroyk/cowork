package com.shiroyk.cowork.coworkgroup.service;

import com.shiroyk.cowork.coworkcommon.constant.ResultCode;
import com.shiroyk.cowork.coworkcommon.dto.APIResponse;
import com.shiroyk.cowork.coworkcommon.dto.UserDto;
import com.shiroyk.cowork.coworkgroup.client.UserFeignClient;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@AllArgsConstructor
public class UserService {
    private final UserFeignClient userFeignClient;

    public APIResponse<?> addUserGroup(String id, String group, boolean force) {
        return userFeignClient.addUserGroup(id, group, force);
    }

    public APIResponse<?> removeUserGroup(String id) {
        return userFeignClient.removeUserGroup(id);
    }

    public UserDto getUser(String id) {
        APIResponse<UserDto> res = userFeignClient.getUser(id);
        if (ResultCode.Ok.equals(res.getCode())) {
            return res.getData();
        }
        return null;
    }

    public Optional<UserDto> getUserInfo(String id) {
        return Optional.of(this.getUser(id));
    }

    public Optional<String> getUserGroup(String id) {
        UserDto userDto = this.getUser(id);
        String group = userDto == null ? null : userDto.getGroup();
        return Optional.ofNullable(group);
    }

    public List<UserDto> getUserList(Set<String> idList) {
        APIResponse<List<UserDto>> userRes = userFeignClient.getUserList(idList);
        if (ResultCode.Ok.equals(userRes.getCode())) {
            return userRes.getData();
        }
        return new ArrayList<>();
    }

    public List<UserDto> getGroupUserList(String group, Integer page, Integer size) {
        APIResponse<List<UserDto>> userRes = userFeignClient.getUserList(group, page, size);
        if (ResultCode.Ok.equals(userRes.getCode())) {
            return userRes.getData();
        }
        return new ArrayList<>();
    }
}
