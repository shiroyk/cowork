package com.shiroyk.cowork.coworkgroup.controller;

import com.shiroyk.cowork.coworkcommon.constant.ResultCode;
import com.shiroyk.cowork.coworkcommon.dto.APIResponse;
import com.shiroyk.cowork.coworkcommon.dto.GroupDto;
import com.shiroyk.cowork.coworkcommon.dto.UploadDoc;
import com.shiroyk.cowork.coworkcommon.model.group.Group;
import com.shiroyk.cowork.coworkgroup.dto.PutGroup;
import com.shiroyk.cowork.coworkgroup.service.DocService;
import com.shiroyk.cowork.coworkgroup.service.GroupService;
import com.shiroyk.cowork.coworkgroup.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/group")
public class GroupController {
    private final GroupService groupService;
    private final UserService userService;
    private final DocService docService;

    /**
     * @Description: 获取单个群组
     * @param gid 群组Id
     * @param uid 用户Id
     * @return groupDto
     */
    @GetMapping("/{gid}")
    public APIResponse<?> getGroup(@PathVariable String gid, @RequestHeader("X-User-Id") String uid) {
        return this.getUserGroup(uid, gid, group ->
                    APIResponse.ok(group.toGroupDtoM(userService.getUser(group.getLeader()),
                        group.getUsers().size(), group.getDocs().size())));
    }

    /**
     * @Description: 获取群组列表
     * @param uid 用户Id
     * @return groupDto
     */
    @GetMapping()
    public APIResponse<?> getAllGroup(@RequestHeader("X-User-Id") String uid) {
        return userService.getUserInfo(uid)
                .map(userInfo ->
                    APIResponse.ok(groupService
                    .findGroupById(userInfo.getGroup())
                    .map(Group::toGroupDto)
                    .collect(Collectors.toList()))
                ).orElse(APIResponse.badRequest("获取信息失败，请稍后重试！"));
    }

    /**
     * @Description: 用户创建群组
     * @param name 群组名
     * @return 成功或失败信息
     */
    @PostMapping()
    public APIResponse<?> createGroup(@RequestHeader("X-User-Id") String uid, String name) {
        if (StringUtils.isEmpty(name))
            return APIResponse.badRequest("群组名称不能为空!");
        Group group = new Group();
        group.setName(name);
        group.setLeader(uid);
        group.setDocs(Collections.emptySet());
        group.setUsers(Collections.singleton(uid));
        group = groupService.save(group);
        APIResponse<?> res = userService.setUserGroup(uid, group.getId());
        if (!ResultCode.Ok.equals(res.getCode())) {
            groupService.delete(group.getId());
            return res;
        }
        return APIResponse.ok("创建群组成功！");
    }

    /**
     * @Description: 更新群组信息
     * @param uid 用户Id
     * @param putGroup 群组信息
     * @return 成功或失败信息
     */
    @PutMapping("/{gid}")
    public APIResponse<?> updateGroup(@RequestHeader("X-User-Id") String uid,
                                      @PathVariable String gid,
                                      @Valid PutGroup putGroup) {
        return this.getUserGroup(uid, gid, group -> {
                    if (group.getLeader().equals(uid)) {
                        group.setName(putGroup.getName());
                        group.setDescribe(putGroup.getDescribe());
                        if (putGroup.getMemberRole() != null)
                            group.setMemRole(putGroup.getMemberRole());
                        groupService.save(group);
                        return APIResponse.ok("更新群组信息成功！");
                    }
                    return APIResponse.create(ResultCode.Forbidden, "无权访问！");
                });
    }

    /**
     * @Description: 申请加入群组
     * @param uid 用户Id
     * @param gid 群组Id
     * @return 成功或失败信息
     */
    @PostMapping("/{gid}/apply")
    public APIResponse<?> applyGroup(@RequestHeader("X-User-Id") String uid,
                                     @PathVariable String gid) {
        return groupService.findById(gid)
                .map(group -> {
                    if (group.getUsers().contains(uid))
                        return APIResponse.badRequest("已经在该群组中！");
                    if (group.getApply().add(uid)) {
                        groupService.save(group);
                        return APIResponse.ok("发送申请成功，请等待审核！");
                    } else return APIResponse.badRequest("已经发送过申请！");
                }).orElse(APIResponse.badRequest("未找到该群组！"));
    }

    /**
     * @Description: 退出群组
     * @param uid 用户Id
     * @param gid 群组Id
     * @return 成功或失败信息
     */
    @DeleteMapping("/{gid}/exit")
    public APIResponse<?> exitGroup(@RequestHeader("X-User-Id") String uid,
                                    @PathVariable String gid) {
        return this.getUserGroup(uid, gid, group -> {
            if (group.getLeader().equals(uid))
                return APIResponse.badRequest("群主不能直接退出群组！");

            APIResponse<?> res = userService.removeUserGroup(uid, gid);
            if (ResultCode.Ok.equals(res.getCode())) {
                group.getUsers().remove(uid);
                groupService.save(group);
                return APIResponse.ok("成功退出该群组！");
            }
            return APIResponse.badRequest("已经不在群组中！");
        });
    }

    /**
     * @Description: 搜索群组
     * @param name 群组名
     * @return List<GroupDto>
     */
    @GetMapping("/search")
    public APIResponse<List<GroupDto>> searchGroup(@RequestParam(required = false, defaultValue = "", value = "name") String name) {
        return APIResponse.ok(groupService.findGroupsByNameContains(name)
                .stream().map(Group::toGroupDto)
                .collect(Collectors.toList()));
    }

    /**
     * @Description: 获取群组的申请消息
     * @param gid 群组Id
     * @param uid 用户Id
     * @return 用户列表
     */
    @GetMapping("/{gid}/apply")
    public APIResponse<?> getGroupApply(@RequestHeader("X-User-Id") String uid,
                                        @PathVariable String gid) {
        return this.getUserGroup(uid, gid, group -> {
                    if (group.getLeader().equals(uid))
                        return APIResponse.ok(userService.getUserList(group.getApply()));
                    else
                        return APIResponse.badRequest("无权访问！");
                    });
    }

    /**
     * @Description: 同意申请消息
     * @param uid 用户Id
     * @param gid 群组Id
     * @param user 申请用户Id
     * @return 成功或失败消息
     */
    @PostMapping("/{gid}/apply/{user}")
    public APIResponse<?> allowApply(@RequestHeader("X-User-Id") String uid,
                                     @PathVariable String gid,
                                     @PathVariable String user) {
        return this.getUserGroup(uid, gid, group -> {
                    if (group.getLeader().equals(uid)) {
                        if (group.getApply().remove(user)) {
                            APIResponse<?> res = userService.setUserGroup(user, group.getId());
                            if (ResultCode.Ok.equals(res.getCode())) {
                                group.getUsers().add(user);
                                groupService.save(group);
                            }
                            return res;
                        }
                        return APIResponse.badRequest("已经同意该申请！");
                    } else return APIResponse.badRequest("无权访问！");
                });
    }

    /**
     * @Description: 拒绝申请消息
     * @param uid 用户Id
     * @param gid 群组Id
     * @param user 申请用户Id
     * @return 成功或失败消息
     */
    @DeleteMapping("/{gid}/apply/{user}")
    public APIResponse<?> denyApply(@RequestHeader("X-User-Id") String uid,
                                     @PathVariable String gid,
                                     @PathVariable String user) {
        return this.getUserGroup(uid, gid, group -> {
            if (group.getLeader().equals(uid)) {
                if (group.getApply().remove(user)) {
                    groupService.save(group);
                    return APIResponse.ok("拒绝申请成功！");
                }
                return APIResponse.badRequest("该申请不存在！");
            } else return APIResponse.badRequest("无权访问！");
        });
    }

    /**
     * @Description: 获取群组成员
     * @param gid 群组Id
     * @param page 分页
     * @param size 数量
     * @return 成员信息列表
     */
    @GetMapping("/{gid}/user")
    public APIResponse<?> getGroupUser(@RequestHeader("X-User-Id") String uid,
                                       @PathVariable String gid,
                                       @RequestParam(required = false, defaultValue = "0", value = "p") Integer page,
                                       @RequestParam(required = false, defaultValue = "10", value = "s") Integer size) {
        return this.getUserGroup(uid, gid, group -> {
                    int index = size * page;
                    List<String> idList = group.getUsers()
                            .stream().sorted()
                            .collect(Collectors.toList())
                            .subList(index, Math.min(index + size, group.getUsers().size()));
                    return APIResponse.ok(userService.getGroupUserList(idList));
                });
    }

    /**
     * @Description: 移除群组成员
     * @param uid 用户Id
     * @param gid 群组Id
     * @param user 移除用户Id
     * @return 成功或失败信息
     */
    @DeleteMapping("/{gid}/user/{user}")
    public APIResponse<?> removeGroupUser(@RequestHeader("X-User-Id") String uid,
                                          @PathVariable String gid,
                                          @PathVariable String user) {
        return this.getUserGroup(uid, gid, group -> {
                    if (group.getLeader().equals(uid)) {
                        if (uid.equals(user))
                            return APIResponse.badRequest("不能移除自己！");
                        if (group.getUsers().remove(user)) {
                            APIResponse<?> res = userService.removeUserGroup(user, gid);
                            if (ResultCode.Ok.equals(res.getCode()))
                                groupService.save(group);
                            return res;
                        } else return APIResponse.badRequest("用户不在本群组！");
                    } else return APIResponse.badRequest("无权访问！");
                });
    }

    /**
     * @Description: 获取群组文档数量
     * @param uid 用户Id
     * @param gid 群组Id
     * @return 文档数量
     */
    @GetMapping("/{gid}/doc/count")
    public APIResponse<?> countAllDoc(@RequestHeader("X-User-Id") String uid,
                                      @PathVariable String gid) {
        return this.getUserGroup(uid, gid, group -> docService.countAllDoc(group.getId()));
    }

    /**
     * @Description: 获取群组文档
     * @param uid 用户Id
     * @param gid 群组Id
     * @param page 分页
     * @param size 数量
     * @return 文档信息
     */
    @GetMapping("/{gid}/doc")
    public APIResponse<?> getAllDoc(@RequestHeader("X-User-Id") String uid,
                                    @PathVariable String gid,
                                    @RequestParam(required = false, defaultValue = "0", value = "p") Integer page,
                                    @RequestParam(required = false, defaultValue = "10", value = "s") Integer size) {
        return this.getUserGroup(uid, gid, group -> docService.getAllDoc(group.getId(), page, size));
    }

    /**
     * @Description: 创建群组文档
     * @param uid 用户Id
     * @param gid 群组Id
     * @param title 文档名
     * @return 成功或失败信息
     */
    @PostMapping("/{gid}/doc")
    public APIResponse<?> createDoc(@RequestHeader("X-User-Id") String uid,
                                    @PathVariable String gid,
                                    String title) {
        return this.getUserGroup(uid, gid, group -> {
            if (GroupDto.MemberRole.Create.equals(group.getMemRole()) || group.getLeader().equals(uid))
                return docService.createDoc(group.getId(), title);
            return APIResponse.badRequest("没有创建文档的权限！");
        });
    }

    /**
     * @Description: 获取群组被标记为已删除的文档数量
     * @param uid 用户Id
     * @param gid 群组Id
     * @return 数量
     */
    @GetMapping("/{gid}/doc/trash/count")
    public APIResponse<?> countAllTrashDoc(@RequestHeader("X-User-Id") String uid,
                                           @PathVariable String gid) {
        return this.getUserGroup(uid, gid, group -> {
            if (GroupDto.MemberRole.CreateDelete.equals(group.getMemRole()) || group.getLeader().equals(uid))
                return docService.countTrashDoc(group.getId());
            return APIResponse.badRequest("没有获取已删除文档的权限！");
        });
    }

    /**
     * @Description: 获取群组被标记为已删除的文档
     * @param uid 用户Id
     * @param gid 群组Id
     * @param page 分页
     * @param size 数量
     * @return 文档信息
     */
    @GetMapping("/{gid}/doc/trash")
    public APIResponse<?> getTrashDoc(@RequestHeader("X-User-Id") String uid,
                                      @PathVariable String gid,
                                      @RequestParam(required = false, defaultValue = "0", value = "p") Integer page,
                                      @RequestParam(required = false, defaultValue = "10", value = "s") Integer size) {
        return this.getUserGroup(uid, gid, group -> {
            if (GroupDto.MemberRole.CreateDelete.equals(group.getMemRole()) || group.getLeader().equals(uid))
                return docService.getAllTrash(group.getId(), page, size);
            return APIResponse.badRequest("没有获取回收站的权限！");
        });
    }

    /**
     * @Description: 获取单个文档
     * @param uid 用户Id
     * @param gid 群组Id
     * @param did 文档Id
     * @return 文档信息
     */
    @GetMapping("/{gid}/doc/{did}")
    public APIResponse<?> getDoc(@RequestHeader("X-User-Id") String uid,
                                 @PathVariable String gid,
                                 @PathVariable String did) {
        return this.getUserGroup(uid, gid, group -> docService.getDocDto(group.getId(), did));
    }

    /**
     * @Description: 将群组文档标记为已删除
     * @param uid 用户Id
     * @param gid 群组Id
     * @param did 文档Id
     * @return 成功或失败信息
     */
    @DeleteMapping("/{gid}/doc/{did}")
    public APIResponse<?> trashDoc(@RequestHeader("X-User-Id") String uid,
                                   @PathVariable String gid,
                                   @PathVariable String did) {
        return this.getUserGroup(uid, gid, group -> {
            if (GroupDto.MemberRole.CreateDelete.equals(group.getMemRole()) || group.getLeader().equals(uid)) {
                return docService.trashDoc(group.getId(), did);
            }
            return APIResponse.badRequest("没有删除文档的权限！");
        });
    }

    /**
     * @Description: 将群组文档标记为未删除
     * @param uid 用户Id
     * @param gid 群组Id
     * @param did 文档Id
     * @return 成功或失败信息
     */
    @PutMapping("/{gid}/doc/trash/{did}")
    public APIResponse<?> recoveryDoc(@RequestHeader("X-User-Id") String uid,
                                      @PathVariable String gid,
                                      @PathVariable String did) {
        return this.getUserGroup(uid, gid, group -> {
            if (GroupDto.MemberRole.CreateDelete.equals(group.getMemRole()) || group.getLeader().equals(uid)) {
                return docService.recoveryDoc(group.getId(), did);
            }
            return APIResponse.badRequest("没有更新文档的权限！");
        });
    }

    /**
     * @Description: 彻底删除群组文档
     * @param uid 用户Id
     * @param gid 群组Id
     * @param did 文档Id
     * @return 成功或失败信息
     */
    @DeleteMapping("/{gid}/doc/trash/{did}")
    public APIResponse<?> deleteDoc(@RequestHeader("X-User-Id") String uid,
                                    @PathVariable String gid,
                                    @PathVariable String did) {
        return this.getUserGroup(uid, gid, group -> {
            if (GroupDto.MemberRole.CreateDelete.equals(group.getMemRole()) || group.getLeader().equals(uid)) {
                APIResponse<?> res = docService.deleteDoc(group.getId(), did);
                if (ResultCode.Ok.equals(res.getCode())) {
                    group.getDocs().remove(did);
                    groupService.save(group);
                }
                return res;
            }
            return APIResponse.badRequest("没有删除文档的权限！");
        });
    }

    /**
     * @Description: 获取群组文档内容
     * @param uid 用户Id
     * @param gid 群组Id
     * @param did 文档Id
     * @return 文档内容
     */
    @GetMapping("/{gid}/doc/{did}/content")
    public APIResponse<?> getDocContent(@RequestHeader("X-User-Id") String uid,
                                        @PathVariable String gid,
                                        @PathVariable String did) {
        return this.getUserGroup(uid, gid, group -> docService.getDocContent(group.getId(), did));
    }

    /**
     * @Description: 搜索群组标记未删除的文档
     * @param uid 用户Id
     * @param gid 群组Id
     * @param title 文档名
     * @return 文档信息
     */
    @GetMapping("/{gid}/doc/search")
    public APIResponse<?> searchDoc(@RequestHeader("X-User-Id") String uid,
                                    @PathVariable String gid,
                                    @RequestParam(required = false, defaultValue = "", value = "t") String title) {
        return this.getUserGroup(uid, gid, group -> docService.searchDoc(group.getId(), title));
    }

    /**
     * @Description: 搜索群组标记已删除的文档
     * @param uid 用户Id
     * @param gid 群组Id
     * @param title 文档名
     * @return 文档信息
     */
    @GetMapping("/{gid}/doc/trash/search")
    public APIResponse<?> searchTrashDoc(@RequestHeader("X-User-Id") String uid,
                                         @PathVariable String gid,
                                         @RequestParam(required = false, defaultValue = "", value = "t") String title) {
        return this.getUserGroup(uid, gid, group -> {
            if (GroupDto.MemberRole.CreateDelete.equals(group.getMemRole()) || group.getLeader().equals(uid)) {
                return docService.searchTrashDoc(group.getId(), title);
            }
            return APIResponse.badRequest("没有获已删除文档的权限！");
        });
    }

    /**
     * @Description: 上传群组文档
     * @param uid 用户ID
     * @param gid 群组Id
     * @param uploadDoc 上传的文档对象
     * @return 成功或失败信息
     */
    @PostMapping("/{gid}/doc/upload")
    public APIResponse<?> uploadDoc(@RequestHeader("X-User-Id") String uid,
                                    @PathVariable String gid,
                                    @RequestBody UploadDoc uploadDoc) {
        return this.getUserGroup(uid, gid, group -> {
            if (!GroupDto.MemberRole.ReadWrite.equals(group.getMemRole()) || group.getLeader().equals(uid)) {
                return docService.uploadDoc(uid, group.getId(), uploadDoc);
            }
            return APIResponse.badRequest("没有上传(创建)文档的权限！");
        });
    }

    /**
     * @Description: 获取用户群组
     * @param uid 用户Id
     * @param function function interface
     * @return function结果
     */
    private APIResponse<?> getUserGroup(String uid, String gid, Function<Group, APIResponse<?>> function) {
        return groupService.findById(gid)
                .map(group -> {
                    if (group.getUsers().contains(uid))
                        return function.apply(group);
                    return APIResponse.badRequest("不在该群组中！");
                }).orElse(APIResponse.badRequest("未找到该群组！"));
    }

}
