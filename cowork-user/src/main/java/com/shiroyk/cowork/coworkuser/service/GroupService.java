package com.shiroyk.cowork.coworkuser.service;

import com.shiroyk.cowork.coworkcommon.constant.ResultCode;
import com.shiroyk.cowork.coworkcommon.dto.APIResponse;
import com.shiroyk.cowork.coworkcommon.dto.GroupDto;
import com.shiroyk.cowork.coworkuser.client.GroupFeignClient;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class GroupService {
    private final GroupFeignClient groupFeignClient;

    public String getGroup(String id) {
        APIResponse<GroupDto> groupRes = groupFeignClient.getGroup(id);
        if (ResultCode.Ok.equals(groupRes.getCode())) {
            return groupRes.getData().getName();
        }
        return null;
    }
}
