package com.shiroyk.cowork.coworkgroup.controller;

import com.shiroyk.cowork.coworkcommon.constant.ResultCode;
import com.shiroyk.cowork.coworkcommon.dto.APIResponse;
import com.shiroyk.cowork.coworkcommon.dto.DocDto;
import com.shiroyk.cowork.coworkcommon.dto.GroupDto;
import com.shiroyk.cowork.coworkcommon.dto.UserDto;
import com.shiroyk.cowork.coworkgroup.model.Group;
import com.shiroyk.cowork.coworkgroup.service.DocService;
import com.shiroyk.cowork.coworkgroup.service.GroupService;
import com.shiroyk.cowork.coworkgroup.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/admin/group")
@Slf4j
@AllArgsConstructor
public class GroupAdminController {
    private final GroupService groupService;
    private final UserService userService;
    private final DocService docService;

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
     * @param page 分页
     * @param size 数量
     * @return List<GroupDto>
     */
    @GetMapping()
    public APIResponse<List<GroupDto>> getGroupList(@RequestParam(required = false, defaultValue = "0", value = "p") Integer page,
                                                  @RequestParam(required = false, defaultValue = "10", value = "s") Integer size) {
        return APIResponse.ok(groupService.findAll(PageRequest.of(page, size)).map(group -> {
            GroupDto groupDto = group.toGroupDto();
            groupDto.setLeader(userService.getUser(group.getLeader()));
            groupDto.setUser(group.getUsers().size());
            groupDto.setDoc(group.getDocs().size());
            return groupDto;
        }).toList());
    }

    /**
     * @Description: 创建群组
     * @param name 群组名
     * @param leader 群主
     * @return 成功或失败信息
     */
    @PostMapping()
    public APIResponse<?> createGroup(String name, String leader) {
        Group group = new Group();
        group.setName(name);
        group.getUsers().add(leader);
        group.setLeader(leader);
        group.setDocs(Collections.emptySet());
        group.setUsers(Collections.singleton(leader));
        group = groupService.save(group);
        APIResponse<?> res = userService.addUserGroup(leader, group.getId(), true);
        if (!ResultCode.Ok.equals(res.getCode())) {
            groupService.delete(group.getId());
            return res;
        }
        return APIResponse.ok("创建群组成功！");
    }

    /**
     * @Description: 搜索群组
     * @param name 群组名
     * @return List<Group>
     */
    @GetMapping("/search")
    public APIResponse<List<Group>> searchGroup(@RequestParam(required = false, defaultValue = "", value = "n") String name) {
        return APIResponse.ok(groupService.findGroupsByNameContains(name));
    }

    /**
     * @Description: 获取单个群组
     * @param id 群组Id
     * @return Group
     */
    @GetMapping("/{id}")
    public APIResponse<Group> getGroup(@PathVariable String id) {
        return groupService.findById(id)
                .map(APIResponse::ok)
                .orElse(APIResponse.badRequest("群组不存在！"));
    }

    /**
     * @Description: 更新群组信息
     * @param id 群组Id
     * @param name 群组名
     * @param describe 群组简介
     * @param leader 群主
     * @param memberRole 群员权限
     * @return 成功或失败信息
     */
    @PutMapping("/{id}")
    public APIResponse<Object> updateGroup(@PathVariable String id,
                                          String name,
                                          String describe,
                                          String leader,
                                          GroupDto.MemberRole memberRole) {
        return groupService.findById(id)
                .map(group -> {
                    if (!StringUtils.isEmpty(name))
                        group.setName(name);
                    if (!StringUtils.isEmpty(describe))
                        group.setDescribe(describe);
                    if (!StringUtils.isEmpty(leader)) {
                        group.getUsers().add(leader);
                        group.setLeader(leader);
                    }
                    if (memberRole != null)
                        group.setMemRole(memberRole);
                    groupService.save(group);
                    userService.addUserGroup(leader, group.getId(), true);
                    return APIResponse.ok("更新成功！");
                })
                .orElse(APIResponse.badRequest("群组不存在！"));
    }

    /**
     * @Description: 删除群组
     * @param id 群组Id
     * @return 成功或失败信息
     */
    @DeleteMapping("/{id}")
    public APIResponse<Object> deleteGroup(@PathVariable String id) {
        groupService.delete(id);
        return APIResponse.ok("删除群组成功！");
    }

    /**
     * @Description: 获取群组成员
     * @param id 群组Id
     * @param page 分页
     * @param size 数量
     * @return List<UserDto>
     */
    @GetMapping("/{id}/user")
    public APIResponse<List<UserDto>> getGroupUser(@PathVariable String id,
                                                  @RequestParam(required = false, defaultValue = "0", value = "p") Integer page,
                                                  @RequestParam(required = false, defaultValue = "10", value = "s") Integer size) {
        return groupService.findById(id)
                .map(group -> APIResponse.ok(userService.getGroupUserList(group.getId(), page, size)))
                .orElse(APIResponse.badRequest("群组不存在！"));
    }

    /**
     * @Description: 添加群组成员
     * @param id 群组Id
     * @param uid 用户Id
     * @return 成功或失败信息
     */
    @PostMapping("/{id}/user")
    public APIResponse<?> addGroupUser(@PathVariable String id,
                                           String uid) {
        return groupService.findById(id)
                .map(group -> {
                    APIResponse<?> res = userService.addUserGroup(uid, group.getId(), true);
                    if (ResultCode.Ok.equals(res.getCode())) {
                        group.getUsers().add(uid);
                        groupService.save(group);
                    }
                    return res;
                })
                .orElse(APIResponse.badRequest("群组不存在！"));
    }

    /**
     * @Description: 移除群组成员
     * @param id 群组Id
     * @param uid 用户Id
     * @return 成功或失败信息
     */
    @DeleteMapping("/{id}/user/{uid}")
    public APIResponse<?> deleteGroupUser(@PathVariable String id,
                                              @PathVariable String uid) {
        return groupService.findById(id)
                .map(group -> {
                    if (group.getLeader().equals(uid)) {
                        return APIResponse.badRequest("该成员是组长！");
                    }
                    APIResponse<?> res = userService.removeUserGroup(uid);
                    if (ResultCode.Ok.equals(res.getCode())) {
                        group.getUsers().remove(uid);
                        groupService.save(group);
                    }
                    return res;
                })
                .orElse(APIResponse.badRequest("群组不存在！"));
    }

    /**
     * @Description: 获取群组文档
     * @param id 群组Id
     * @param page 分页
     * @param size 数量
     * @return List<DocDto>
     */
    @GetMapping("/{id}/doc")
    public APIResponse<List<DocDto>> getGroupDoc(@PathVariable String id,
                                                @RequestParam(required = false, defaultValue = "0", value = "p") Integer page,
                                                @RequestParam(required = false, defaultValue = "10", value = "s") Integer size) {
        return groupService.findById(id)
                .map(group -> docService.getAllDoc(group.getId(), page, size))
                .orElse(APIResponse.badRequest("群组不存在！"));
    }

    /**
     * @Description: 添加群组文档
     * @param id 群组Id
     * @param did 文档Id
     * @return 成功或失败信息
     */
    @PostMapping("/{id}/doc")
    public APIResponse<Object> addGroupDoc(@PathVariable String id,
                                          String did) {
        return groupService.findById(id)
                .map(group -> {
                    if (group.getDocs().add(did)) {
                        if (ResultCode.Ok.equals(docService.updateDocOwner(group.getId(), did, id))) {
                            groupService.save(group);
                            return APIResponse.ok("添加文档成功！");
                        }
                        return APIResponse.ok("添加文档失败！");
                    }
                    return APIResponse.ok("已有该文档！");
                })
                .orElse(APIResponse.badRequest("群组不存在！"));
    }

    /**
     * @Description: 删除群组文档
     * @param id 群组Id
     * @param did 文档Id
     * @return 成功或失败信息
     */
    @DeleteMapping("/{id}/doc/{did}")
    public APIResponse<?> deleteGroupDoc(@PathVariable String id,
                                             @PathVariable String did) {
        return groupService.findById(id)
                .map(group -> {
                    APIResponse<?> res = docService.deleteDoc(group.getId(), did);
                    if (ResultCode.Ok.equals(res.getCode())) {
                        group.getDocs().remove(did);
                        groupService.save(group);
                        return res;
                    }
                    return APIResponse.ok("删除文档失败！");
                })
                .orElse(APIResponse.badRequest("群组不存在！"));
    }
}
