package com.shiroyk.cowork.coworkgroup.controller;

import com.shiroyk.cowork.coworkcommon.constant.ResultCode;
import com.shiroyk.cowork.coworkcommon.dto.APIResponse;
import com.shiroyk.cowork.coworkcommon.dto.GroupDto;
import com.shiroyk.cowork.coworkgroup.service.GroupService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/client")
public class GroupClientController {
    private final GroupService groupService;

    /**
     * @Description: 获取群组数量
     * @return Long
     */
    @GetMapping("/count")
    public APIResponse<Long> getGroupSize() {
        return APIResponse.ok(groupService.count());
    }

    /**
     * @Description: 获取群组
     * @param id 群组Id
     * @return GroupDto
     */
    @GetMapping("/{id}")
    public APIResponse<GroupDto> getGroup(@PathVariable String id) {
        return groupService.findById(id)
                .map(group -> APIResponse.ok(group.toGroupDto()))
                .orElse(APIResponse.create(ResultCode.Forbidden, "无权访问！"));
    }

    /**
     * @Description: 获取群组用户Id
     * @param id 群组Id
     * @return Set<String>
     */
    @GetMapping("/{id}/users")
    public APIResponse<Set<String>> getUser(@PathVariable String id) {
        return groupService.findById(id)
                .map(group -> APIResponse.ok(group.getUsers()))
                .orElse(APIResponse.create(ResultCode.Forbidden, "无权访问！"));
    }

    /**
     * @Description: 添加群组文档
     * @param id 群组Id
     * @param did 文档Id
     * @return 成功或失败信息
     */
    @PutMapping("/{id}")
    public APIResponse<?> addGroupDoc(@PathVariable String id, String did) {
        return groupService.findById(id)
                .map(group -> {
                    group.getDocs().add(did);
                    groupService.save(group);
                    return APIResponse.ok("添加文档成功！");
                }).orElse(APIResponse.create(ResultCode.Forbidden, "添加文档失败！"));
    }
}
