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

    public APIResponse<?> setUserGroup(String id, String group) {
        return userFeignClient.setUserGroup(id, group);
    }

    public APIResponse<?> removeUserGroup(String id, String group) {
        return userFeignClient.removeUserGroup(id, group);
    }

    public APIResponse<?> removeUserListGroup(String group, Set<String> idList) {
        return userFeignClient.removeUserListGroup(group, idList);
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

    public List<UserDto> getUserList(Set<String> idList) {
        APIResponse<List<UserDto>> userRes = userFeignClient.getUserList(idList);
        if (ResultCode.Ok.equals(userRes.getCode())) {
            return userRes.getData();
        }
        return new ArrayList<>();
    }

    public List<UserDto> getGroupUserList(List<String> idList) {
        APIResponse<List<UserDto>> userRes = userFeignClient.getUserDetailList(idList);
        if (ResultCode.Ok.equals(userRes.getCode())) {
            return userRes.getData();
        }
        return new ArrayList<>();
    }
}
