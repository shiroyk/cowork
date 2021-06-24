package com.shiroyk.cowork.coworkadmin.controller;

import com.shiroyk.cowork.coworkadmin.dto.UpdateGroup;
import com.shiroyk.cowork.coworkadmin.service.DocService;
import com.shiroyk.cowork.coworkadmin.service.GroupService;
import com.shiroyk.cowork.coworkadmin.service.UserService;
import com.shiroyk.cowork.coworkcommon.dto.APIResponse;
import com.shiroyk.cowork.coworkcommon.dto.GroupDto;
import com.shiroyk.cowork.coworkcommon.dto.UserDto;
import com.shiroyk.cowork.coworkcommon.model.doc.Doc;
import com.shiroyk.cowork.coworkcommon.model.doc.Owner;
import com.shiroyk.cowork.coworkcommon.model.group.Group;
import com.shiroyk.cowork.coworkcommon.model.user.User;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/group")
@Slf4j
@AllArgsConstructor
public class GroupController {
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
            userService.findById(group.getLeader())
                    .ifPresent(u -> groupDto.setLeader(u.toUserDtoM()));
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
        groupService.save(group);
        return userService.findById(leader)
                .map(user -> {
                    user.getGroup().add(group.getId());
                    return APIResponse.ok("创建群组成功！");
                })
        .orElse(APIResponse.badRequest("用户不存在！"));
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
     * @param updateGroup 群组信息
     * @return 成功或失败信息
     */
    @PutMapping("/{id}")
    public APIResponse<Object> updateGroup(@PathVariable String id,
                                          @Valid UpdateGroup updateGroup) {
        return groupService.findById(id)
                .map(group -> {

                    Optional<User> user = userService.findById(updateGroup.getLeader());

                    if (!user.isPresent()) return APIResponse.badRequest("用户不存在!");

                    group.setName(updateGroup.getName());
                    group.setDescribe(updateGroup.getDescribe());
                    group.getUsers().add(updateGroup.getLeader());
                    group.setLeader(updateGroup.getLeader());
                    group.setMemRole(updateGroup.getMemberRole());
                    groupService.save(group);

                    user.ifPresent(u -> {
                        u.getGroup().add(group.getId());
                        userService.save(u);
                    });
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

                    List<User> userList = userService.findUserByIdList(group.getUsers())
                            .peek(user -> user.getGroup().remove(group.getId()))
                            .collect(Collectors.toList());
                    userService.saveAll(userList);

                    // 群组文档也要删除，这里先不写
                    groupService.delete(gid);
                    return APIResponse.ok("删除群组成功！");
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
                    return APIResponse.ok(userService.findUserByIdList(idList)
                            .map(User::toUserDtoL).collect(Collectors.toList()));
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
                .map(group ->
                        userService.findById(uid)
                        .map(user -> {
                            user.getGroup().add(group.getId());
                            userService.save(user);
                            group.getUsers().add(user.getId());
                            groupService.save(group);
                            return APIResponse.ok("添加成功!");
                        }).orElse(APIResponse.badRequest("用户不存在!")))
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
                    return userService.findById(uid)
                            .map(user -> {
                                user.getGroup().remove(group.getId());
                                userService.save(user);
                                group.getUsers().remove(user.getId());
                                groupService.save(group);
                                return APIResponse.ok("添加成功!");
                            }).orElse(APIResponse.badRequest("用户不存在!"));
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
    public APIResponse<List<Doc>> getGroupDoc(@PathVariable String gid,
                                              @RequestParam(required = false, defaultValue = "0", value = "p") Integer page,
                                              @RequestParam(required = false, defaultValue = "10", value = "s") Integer size) {
        return groupService.findById(gid)
                .map(group -> APIResponse.ok(docService.findDocsByDeleteIsFalse(group.getId(), PageRequest.of(page, size))))
                .orElse(APIResponse.ok(Collections.emptyList()));
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
                .map(group -> {
                    Doc doc = new Doc();
                    doc.setTitle(title);
                    doc.setOwner(new Owner(gid, Owner.OwnerEnum.Group));
                    docService.save(doc);
                    return APIResponse.ok("创建成功!");
                })
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
                .map(group ->
                        docService.findById(did).map(doc -> {
                            doc.setDelete(true);
                            docService.save(doc);
                            return APIResponse.ok("文档放入回收站成功！");
                        }).orElse(APIResponse.badRequest("文档不存在！")))
                .orElse(APIResponse.badRequest("群组不存在！"));
    }
}
