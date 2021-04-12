package com.shiroyk.cowork.coworkdoc.service;

import com.shiroyk.cowork.coworkcommon.constant.ResultCode;
import com.shiroyk.cowork.coworkcommon.dto.APIResponse;
import com.shiroyk.cowork.coworkcommon.dto.GroupDto;
import com.shiroyk.cowork.coworkdoc.client.GroupFeignClient;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@AllArgsConstructor
public class GroupService {
    private final GroupFeignClient groupFeignClient;

    public Long getGroupSize() {
        APIResponse<Long> res = groupFeignClient.getGroupSize();
        if (ResultCode.Ok.equals(res.getCode()))
            return res.getData();
        return 0L;
    }

    public List<String> getUsers(String id) {
        APIResponse<List<String>> res = groupFeignClient.getUsers(id);
        if (ResultCode.Ok.equals(res.getCode()))
            return res.getData();
        return Collections.emptyList();
    }

    public boolean existUser(String group, String uid) {
        return group != null && getUsers(group).contains(uid);
    }

    public GroupDto getGroup(String id) {
        APIResponse<GroupDto> res = groupFeignClient.getGroup(id);
        if (ResultCode.Ok.equals(res.getCode()))
            return res.getData();
        return null;
    }

    public boolean groupExist(String id) {
        return getGroup(id) != null;
    }

    public void addGroupDoc(String id, String did) {
        groupFeignClient.addGroupDoc(id, did);
    }

}
