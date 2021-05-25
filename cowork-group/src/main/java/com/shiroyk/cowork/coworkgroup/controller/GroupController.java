package com.shiroyk.cowork.coworkgroup.controller;

import com.shiroyk.cowork.coworkcommon.constant.ResultCode;
import com.shiroyk.cowork.coworkcommon.dto.APIResponse;
import com.shiroyk.cowork.coworkcommon.dto.GroupDto;
import com.shiroyk.cowork.coworkcommon.dto.UploadDoc;
import com.shiroyk.cowork.coworkgroup.dto.PutGroup;
import com.shiroyk.cowork.coworkgroup.model.Group;
import com.shiroyk.cowork.coworkgroup.service.DocService;
import com.shiroyk.cowork.coworkgroup.service.GroupService;
import com.shiroyk.cowork.coworkgroup.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
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
     * @Description: 获取群组
     * @param uid 用户Id
     * @return groupDto
     */
    @GetMapping()
    public APIResponse<?> getGroup(@RequestHeader("X-User-Id") String uid) {
        return this.getUserGroup(uid, group -> {
            GroupDto groupDto = group.toGroupDto();
            groupDto.setLeader(userService.getUser(group.getLeader()));
            groupDto.setUser(group.getUsers().size());
            groupDto.setDoc(group.getDocs().size());
            return APIResponse.ok(groupDto);
        });
    }

    /**
     * @Description: 更新群组信息
     * @param id 用户Id
     * @param putGroup 群组信息
     * @return 成功或失败信息
     */
    @PutMapping()
    public APIResponse<?> updateGroup(@RequestHeader("X-User-Id") String id,
                                          @Valid PutGroup putGroup) {
        return this.getUserGroup(id, group -> {
            if (group.getLeader().equals(id)) {
                group.setName(putGroup.getName());
                group.setDescribe(putGroup.getDescribe());
                if (putGroup.getMemberRole() != null)
                    group.setMemRole(putGroup.getMemberRole());
                groupService.save(group);
                return APIResponse.ok("更新成功！");
            }
            return APIResponse.create(ResultCode.Forbidden, "无权访问！");
        });
    }

    /**
     * @Description: 申请加入群组
     * @param uid 用户Id
     * @param did 文档Id
     * @return 成功或失败信息
     */
    @PostMapping("/apply")
    public APIResponse<?> applyGroup(@RequestHeader("X-User-Id") String uid, String did) {
        return userService.getUserGroup(uid)
                .map(tid -> APIResponse.badRequest("暂不支持加入多个群组！"))
                .orElseGet(() ->
                        groupService.findById(did)
                        .map(group -> {
                            if (group.getApply().add(uid)) {
                                groupService.save(group);
                                return APIResponse.ok("发送申请成功，请等待审核！");
                            } else return APIResponse.badRequest("已经发送过申请！");
                        })
                        .orElse(APIResponse.badRequest("群组不存在或已解散！")));
    }

    /**
     * @Description: 退出群组
     * @param uid 用户Id
     * @return 成功或失败信息
     */
    @DeleteMapping("/exit")
    public APIResponse<?> exitGroup(@RequestHeader("X-User-Id") String uid) {
        return this.getUserGroup(uid, group -> {
            if (group.getLeader().equals(uid))
                return APIResponse.badRequest("群主不能直接退出群组，请解散群组！");
            if (group.getUsers().remove(uid)) {
                userService.removeUserGroup(uid);
                groupService.save(group);
                return APIResponse.ok("成功推出该群组！");
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
     * @param uid 用户Id
     * @return 用户列表
     */
    @GetMapping("/apply")
    public APIResponse<?> getGroupApply(@RequestHeader("X-User-Id") String uid) {
        return this.getUserGroup(uid, group -> {
            if (group.getLeader().equals(uid))
                return APIResponse.ok(userService.getUserList(group.getApply()));
            else
                return APIResponse.badRequest("无权访问！");
        });
    }

    /**
     * @Description: 同意申请消息
     * @param id 用户Id
     * @param uid 申请用户Id
     * @return 成功或失败消息
     */
    @PostMapping("/user")
    public APIResponse<?> allowApply(@RequestHeader("X-User-Id") String id,
                                      String uid) {
        return this.getUserGroup(id, group -> {
            if (group.getLeader().equals(id)) {
                if (group.getApply().remove(uid)) {
                    APIResponse<?> res = userService.addUserGroup(uid, group.getId(), false);
                    if (ResultCode.Ok.equals(res.getCode())) {
                        group.getUsers().add(uid);
                        groupService.save(group);
                    }
                    return res;
                } else {
                    return APIResponse.badRequest("已经同意该申请！");
                }
            } else return APIResponse.badRequest("无权访问！");
        });
    }

    /**
     * @Description: 获取群组成员
     * @param uid 用户Id
     * @param page 分页
     * @param size 数量
     * @return 成员信息列表
     */
    @GetMapping("/user")
    public APIResponse<?> getGroupUser(@RequestHeader("X-User-Id") String uid,
                                      @RequestParam(required = false, defaultValue = "0", value = "p") Integer page,
                                      @RequestParam(required = false, defaultValue = "10", value = "s") Integer size) {
        return this.getUserGroup(uid, group -> APIResponse.ok(userService.getGroupUserList(group.getId(), page, size)));
    }

    /**
     * @Description: 移除群组成员
     * @param id 用户Id
     * @param uid 移除用户Id
     * @return 成功或失败信息
     */
    @DeleteMapping("/user/{uid}")
    public APIResponse<?> removeGroupUser(@RequestHeader("X-User-Id") String id,
                                         @PathVariable String uid) {
        return this.getUserGroup(id, group -> {
            if (group.getLeader().equals(id)) {
                if (id.equals(uid))
                    return APIResponse.badRequest("不能移除自己！");

                if (group.getUsers().remove(uid)) {
                    groupService.save(group);
                    return userService.removeUserGroup(uid);
                } else return APIResponse.badRequest("用户不在本群组！");

            } else return APIResponse.badRequest("无权访问！");
        });
    }

    /**
     * @Description: 获取群组文档数量
     * @param uid 用户Id
     * @return 文档数量
     */
    @GetMapping("/doc/count")
    public APIResponse<?> countAllDoc(@RequestHeader("X-User-Id") String uid) {
        return this.getUserGroup(uid, group -> docService.countAllDoc(group.getId()));
    }

    /**
     * @Description: 获取群组文档
     * @param uid 用户Id
     * @param page 分页
     * @param size 数量
     * @return 文档信息
     */
    @GetMapping("/doc")
    public APIResponse<?> getAllDoc(@RequestHeader("X-User-Id") String uid,
                                               @RequestParam(required = false, defaultValue = "0", value = "p") Integer page,
                                               @RequestParam(required = false, defaultValue = "10", value = "s") Integer size) {
        return this.getUserGroup(uid, group -> docService.getAllDoc(group.getId(), page, size));
    }

    /**
     * @Description: 创建群组文档
     * @param uid 用户Id
     * @param title 文档名
     * @return 成功或失败信息
     */
    @PostMapping("/doc")
    public APIResponse<?> createDoc(@RequestHeader("X-User-Id") String uid, String title) {
        return this.getUserGroup(uid, group -> {
            if (GroupDto.MemberRole.Create.equals(group.getMemRole()) || group.getLeader().equals(uid))
                return docService.createDoc(group.getId(), title);
            return APIResponse.badRequest("没有创建文档的权限！");
        });
    }

    /**
     * @Description: 获取群组被标记为已删除的文档数量
     * @param uid 用户Id
     * @return 数量
     */
    @GetMapping("/doc/trash/count")
    public APIResponse<?> countAllTrashDoc(@RequestHeader("X-User-Id") String uid) {
        return this.getUserGroup(uid, group -> {
            if (GroupDto.MemberRole.CreateDelete.equals(group.getMemRole()) || group.getLeader().equals(uid))
                return docService.countTrashDoc(group.getId());
            return APIResponse.badRequest("没有获取已删除文档的权限！");
        });
    }

    /**
     * @Description: 获取群组被标记为已删除的文档
     * @param uid 用户Id
     * @param page 分页
     * @param size 数量
     * @return 文档信息
     */
    @GetMapping("/doc/trash")
    public APIResponse<?> getTrashDoc(@RequestHeader("X-User-Id") String uid,
                                      @RequestParam(required = false, defaultValue = "0", value = "p") Integer page,
                                      @RequestParam(required = false, defaultValue = "10", value = "s") Integer size) {
        return this.getUserGroup(uid, group -> {
            if (GroupDto.MemberRole.CreateDelete.equals(group.getMemRole()) || group.getLeader().equals(uid))
                return docService.getAllTrash(group.getId(), page, size);
            return APIResponse.badRequest("没有获取已删除文档的权限！");
        });
    }

    /**
     * @Description: 获取单个文档
     * @param uid 用户Id
     * @param did 文档Id
     * @return 文档信息
     */
    @GetMapping("/doc/{did}")
    public APIResponse<?> getDoc(@RequestHeader("X-User-Id") String uid, @PathVariable String did) {
        return this.getUserGroup(uid, group -> docService.getDocDto(group.getId(), did));
    }

    /**
     * @Description: 将群组文档标记为已删除
     * @param uid 用户Id
     * @param did 文档Id
     * @return 成功或失败信息
     */
    @DeleteMapping("/doc/{did}")
    public APIResponse<?> trashDoc(@RequestHeader("X-User-Id") String uid, @PathVariable String did) {
        return this.getUserGroup(uid, group -> {
            if (GroupDto.MemberRole.CreateDelete.equals(group.getMemRole()) || group.getLeader().equals(uid)) {
                return docService.trashDoc(group.getId(), did);
            }
            return APIResponse.badRequest("没有删除文档的权限！");
        });
    }

    /**
     * @Description: 将群组文档标记为未删除
     * @param uid 用户Id
     * @param did 文档Id
     * @return 成功或失败信息
     */
    @PutMapping("/doc/trash/{did}")
    public APIResponse<?> recoveryDoc(@RequestHeader("X-User-Id") String uid, @PathVariable String did) {
        return this.getUserGroup(uid, group -> {
            if (GroupDto.MemberRole.CreateDelete.equals(group.getMemRole()) || group.getLeader().equals(uid)) {
                return docService.recoveryDoc(group.getId(), did);
            }
            return APIResponse.badRequest("没有更新文档的权限！");
        });
    }

    /**
     * @Description: 彻底删除群组文档
     * @param uid 用户Id
     * @param did 文档Id
     * @return 成功或失败信息
     */
    @DeleteMapping("/doc/trash/{did}")
    public APIResponse<?> deleteDoc(@RequestHeader("X-User-Id") String uid, @PathVariable String did) {
        return this.getUserGroup(uid, group -> {
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
     * @param did 文档Id
     * @return 文档内容
     */
    @GetMapping("/doc/{did}/content")
    public APIResponse<?> getDocContent(@RequestHeader("X-User-Id") String uid, @PathVariable String did) {
        return this.getUserGroup(uid, group -> docService.getDocContent(group.getId(), did));
    }

    /**
     * @Description: 搜索群组标记未删除的文档
     * @param uid 用户Id
     * @param title 文档名
     * @return 文档信息
     */
    @GetMapping("/doc/search")
    public APIResponse<?> searchDoc(@RequestHeader("X-User-Id") String uid,
                                    @RequestParam(required = false, defaultValue = "", value = "t") String title) {
        return this.getUserGroup(uid, group -> docService.searchDoc(group.getId(), title));
    }

    /**
     * @Description: 搜索群组标记已删除的文档
     * @param uid 用户Id
     * @param title 文档名
     * @return 文档信息
     */
    @GetMapping("/doc/trash/search")
    public APIResponse<?> searchTrashDoc(@RequestHeader("X-User-Id") String uid,
                                         @RequestParam(required = false, defaultValue = "", value = "t") String title) {
        return this.getUserGroup(uid, group -> {
            if (GroupDto.MemberRole.CreateDelete.equals(group.getMemRole()) || group.getLeader().equals(uid)) {
                return docService.searchTrashDoc(group.getId(), title);
            }
            return APIResponse.badRequest("没有获已删除文档的权限！");
        });
    }

    /**
     * @Description: 上传群组文档
     * @param uid 用户ID
     * @param uploadDoc 上传的文档对象
     * @return 成功或失败信息
     */
    @PostMapping("/doc/uploadDoc")
    public APIResponse<?> uploadDoc(@RequestHeader("X-User-Id") String uid,
                                    @RequestBody UploadDoc uploadDoc) {
        return this.getUserGroup(uid, group -> {
            if (GroupDto.MemberRole.CreateDelete.equals(group.getMemRole()) || group.getLeader().equals(uid)) {
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
    private APIResponse<?> getUserGroup(String uid, Function<Group, APIResponse<?>> function) {
        return userService.getUserGroup(uid)
                .map(tid -> groupService.findById(tid)
                        .map(function)
                        .orElse(APIResponse.badRequest("群组不存在或已解散！")))
                .orElse(APIResponse.badRequest("尚未加入群组！"));
    }

}
