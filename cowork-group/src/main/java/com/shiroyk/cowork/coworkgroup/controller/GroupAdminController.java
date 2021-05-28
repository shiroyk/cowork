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
import java.util.stream.Collectors;

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
        group.setLeader(leader);
        group.setDocs(Collections.emptySet());
        group.setUsers(Collections.singleton(leader));
        group = groupService.save(group);
        APIResponse<?> res = userService.setUserGroup(leader, group.getId());
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
                    userService.setUserGroup(leader, group.getId());
                    return APIResponse.ok("更新成功！");
                })
                .orElse(APIResponse.badRequest("群组不存在！"));
    }

    /**
     * @Description: 删除群组
     * @param gid 群组Id
     * @return 成功或失败信息
     */
    @DeleteMapping("/{gid}")
    public APIResponse<?> deleteGroup(@PathVariable String gid) {
        return groupService.findById(gid)
                .map(group -> {
                    // 把用户从群组移除
                    APIResponse<?> res =  userService.removeUserListGroup(gid, group.getUsers());
                    if (ResultCode.Ok.equals(res.getCode())) {
                        // 群组文档也要删除，这里先不写
                        groupService.delete(gid);
                        return APIResponse.ok("删除群组成功！");
                    }
                    return APIResponse.ok("删除群组失败，请稍后重试！");
                }).orElse(APIResponse.badRequest("群组不存在！"));
    }

    /**
     * @Description: 获取群组成员
     * @param gid 群组Id
     * @param page 分页
     * @param size 数量
     * @return List<UserDto>
     */
    @GetMapping("/{gid}/user")
    public APIResponse<List<UserDto>> getGroupUser(@PathVariable String gid,
                                                  @RequestParam(required = false, defaultValue = "0", value = "p") Integer page,
                                                  @RequestParam(required = false, defaultValue = "10", value = "s") Integer size) {
        return groupService.findById(gid)
                .map(group -> {
                    int index = size * page;
                    List<String> idList = group.getUsers()
                            .stream().sorted()
                            .collect(Collectors.toList())
                            .subList(index, Math.min(index + size, group.getUsers().size()));
                    return APIResponse.ok(userService.getGroupUserList(idList));
                })
                .orElse(APIResponse.badRequest("群组不存在！"));
    }

    /**
     * @Description: 添加群组成员
     * @param gid 群组Id
     * @param uid 用户Id
     * @return 成功或失败信息
     */
    @PostMapping("/{gid}/user")
    public APIResponse<?> addGroupUser(@PathVariable String gid,
                                           String uid) {
        return groupService.findById(gid)
                .map(group -> {
                    APIResponse<?> res = userService.setUserGroup(uid, group.getId());
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
     * @param gid 群组Id
     * @param uid 用户Id
     * @return 成功或失败信息
     */
    @DeleteMapping("/{gid}/user/{uid}")
    public APIResponse<?> deleteGroupUser(@PathVariable String gid,
                                          @PathVariable String uid) {
        return groupService.findById(gid)
                .map(group -> {
                    if (group.getLeader().equals(uid)) {
                        return APIResponse.badRequest("该成员是组长！");
                    }
                    APIResponse<?> res = userService.removeUserGroup(uid, gid);
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
     * @param gid 群组Id
     * @param page 分页
     * @param size 数量
     * @return List<DocDto>
     */
    @GetMapping("/{gid}/doc")
    public APIResponse<List<DocDto>> getGroupDoc(@PathVariable String gid,
                                                @RequestParam(required = false, defaultValue = "0", value = "p") Integer page,
                                                @RequestParam(required = false, defaultValue = "10", value = "s") Integer size) {
        return groupService.findById(gid)
                .map(group -> docService.getAllDoc(group.getId(), page, size))
                .orElse(APIResponse.badRequest("群组不存在！"));
    }

    /**
     * @Description: 添加群组文档
     * @param gid 群组Id
     * @param title 文档名称
     * @return 成功或失败信息
     */
    @PostMapping("/{gid}/doc")
    public APIResponse<?> addGroupDoc(@PathVariable String gid,
                                          String title) {
        return groupService.findById(gid)
                .map(group -> docService.createDoc(group.getId(), title))
                .orElse(APIResponse.badRequest("群组不存在！"));
    }

    /**
     * @Description: 删除群组文档
     * @param gid 群组Id
     * @param did 文档Id
     * @return 成功或失败信息
     */
    @DeleteMapping("/{gid}/doc/{did}")
    public APIResponse<?> deleteGroupDoc(@PathVariable String gid,
                                         @PathVariable String did) {
        return groupService.findById(gid)
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
