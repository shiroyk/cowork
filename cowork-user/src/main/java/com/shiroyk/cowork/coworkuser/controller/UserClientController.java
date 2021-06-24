package com.shiroyk.cowork.coworkuser.controller;

import com.shiroyk.cowork.coworkcommon.constant.Role;
import com.shiroyk.cowork.coworkcommon.dto.APIResponse;
import com.shiroyk.cowork.coworkcommon.dto.UserDto;
import com.shiroyk.cowork.coworkcommon.model.user.RecentDoc;
import com.shiroyk.cowork.coworkcommon.model.user.User;
import com.shiroyk.cowork.coworkuser.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/client")
public class UserClientController {
    private final UserService userService;

    /**
     * @Description: 获取用户数量
     * @return Long
     */
    @GetMapping("/count")
    public APIResponse<Long> getUserSize() {
        return APIResponse.ok(userService.count());
    }

    /**
     * @Description: 获取单个用户
     * @param id 用户Id
     * @return 用户信息
     */
    @GetMapping("/{id}")
    public APIResponse<UserDto> getUser(@PathVariable String id) {
        return userService.findById(id)
                .map(user -> APIResponse.ok(user.toUserDtoL()))
                .orElse(APIResponse.badRequest("用户不存在！"));
    }

    /**
     * @Description: 获取用户收藏的文档
     * @param id 用户Id
     * @return 文档Id列表
     */
    @GetMapping("/{id}/star")
    public APIResponse<Set<String>> getUserDocStar(@PathVariable String id) {
        return userService.findById(id)
                .map(user -> APIResponse.ok(user.getStar()))
                .orElse(APIResponse.badRequest("用户不存在！"));
    }

    /**
     * @Description: 添加用户收藏文档
     * @param id 用户Id
     * @param docId 文档Id
     * @return 成功或失败信息
     */
    @PutMapping("/{id}/star")
    public APIResponse<?> newUserDocStar(@PathVariable String id, String docId) {
        return userService.findById(id)
                .map(user -> {
                    if (user.getStar().add(docId)) {
                        userService.save(user);
                        return APIResponse.ok("添加到我的收藏成功！");
                    } else {
                        user.getStar().remove(docId);
                        userService.save(user);
                        return APIResponse.ok("成功取消收藏文档！");
                    }
                })
                .orElse(APIResponse.badRequest("用户不存在！"));
    }

    /**
     * @Description: 删除用户收藏的文档
     * @param id 用户Id
     * @param docId 文档Id
     * @return 成功或失败信息
     */
    @DeleteMapping("/{id}/star")
    public APIResponse<?> deleteUserDocStar(@PathVariable String id, String docId) {
        return userService.findById(id)
                .map(user -> {
                    if (user.getStar().remove(docId)) {
                        userService.save(user);
                        return APIResponse.badRequest("成功取消收藏文档！");
                    }
                    return APIResponse.ok("文档不在我的收藏中！");
                })
                .orElse(APIResponse.badRequest("用户不存在！"));
    }

    /**
     * @Description: 获取用户列表(详情信息)
     * @param idList 用户Id列表
     * @return 用户详情信息列表
     */
    @PostMapping("/detailList")
    public APIResponse<List<UserDto>> getUserDetailList(@RequestBody List<String> idList) {
        return APIResponse.ok(userService.findUserByIdList(idList).map(User::toUserDtoM).collect(Collectors.toList()));
    }

    /**
     * @Description: 获取用户列表(简略信息)
     * @param idList 用户Id列表
     * @return 用户简略信息列表
     */
    @PostMapping("/list")
    public APIResponse<List<UserDto>> getUserList(@RequestBody List<String> idList) {
        return APIResponse.ok(userService.findUserByIdList(idList).map(User::toUserDtoL).collect(Collectors.toList()));
    }

    /**
     * @Description: 设置用户群组
     * @param uid 用户Id
     * @param group 群组Id
     * @return 成功或失败信息
     */
    @PostMapping("/{uid}/group")
    public APIResponse<?> setUserGroup(@PathVariable String uid, String group) {
        return userService.findById(uid)
                .map(user -> {
                    if (user.getGroup().add(group)) {
                        userService.save(user);
                        return APIResponse.ok("用户加入群组成功！");
                    }
                    return APIResponse.badRequest("用户已加入该群组！");
                })
                .orElse(APIResponse.badRequest("用户不存在！"));
    }

    /**
     * @Description: 将用户从群组移除
     * @param uid 用户Id
     * @param group 群组Id
     * @return 成功或失败信息
     */
    @DeleteMapping("/{uid}/group")
    public APIResponse<?> removeUserGroup(@PathVariable String uid, String group) {
        return userService.findById(uid)
                .map(user -> {
                    if (user.getGroup().remove(group)) {
                        userService.save(user);
                        return APIResponse.ok("移出群组成功！");
                    }
                    return APIResponse.badRequest("用户未加入该群组！");
                })
                .orElse(APIResponse.badRequest("用户不存在！"));
    }

    /**
     * @Description: 获取用户最近访问的文档
     * @param uid 用户Id
     */
    @GetMapping("/{uid}/recent")
    public APIResponse<?> getUserRecentDoc(@PathVariable String uid) {
        return userService.findById(uid)
                .map(user -> APIResponse.ok(user.getRecent()
                        .stream().sorted(Comparator.reverseOrder())
                        .map(RecentDoc::getDocId)
                        .collect(Collectors.toList())))
                .orElse(APIResponse.badRequest("用户不存在！"));
    }

    /**
     * @Description: 添加用户最近访问的文档
     * @param uid 用户Id
     * @param did 文档Id
     */
    @PostMapping("/{uid}/recent")
    public APIResponse<?> addUserRecentDoc(@PathVariable String uid,
                                           String did) {
        return userService.findById(uid)
                .map(user -> {
                    Set<RecentDoc> recent = user.getRecent().stream().sorted()
                            .limit(9).collect(Collectors.toSet());
                    recent.add(new RecentDoc(did));
                    user.setRecent(recent);
                    userService.save(user);
                    return APIResponse.ok();
                })
                .orElse(APIResponse.badRequest("用户不存在！"));
    }

    /**
     * @Description: 创建用户
     * @param username 用户名
     * @param email 邮箱
     * @param password 密码
     * @return 成功或失败信息
     */
    @PostMapping("/signup")
    public APIResponse<?> createUser(String username,
                                          String email,
                                          String password) {
        return userService.findUserByName(username)
                .map(user -> APIResponse.badRequest("用户名已存在！"))
                .orElseGet(() -> {
                    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
                    User user = new User(username, email, encoder.encode(password), Role.Normal);
                    if (userService.save(user) != null)
                        return APIResponse.ok("注册成功！");
                    else
                        return APIResponse.badRequest("注册失败，请稍后重试！");
                });
    }

    /**
     * @Description: 获取用户信息
     * @param name 用户名
     * @return 用户信息
     */
    @PostMapping("/info")
    public APIResponse<User> getUserInfo(String name) {
        return userService.findUserByName(name)
                .map(APIResponse::ok)
                .orElse(APIResponse.badRequest("未找到用户"));
    }

    /**
     * @Description: 重置用户密码
     * @param username 用户名
     * @param password 密码
     * @return 成功或失败信息
     */
    @PostMapping("/reset")
    public APIResponse<?> resetPassword(String username,
                                             String password) {
        return userService.findUserByName(username)
                .map(user -> {
                    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
                    user.setPassword(encoder.encode(password));
                    userService.save(user);
                    return APIResponse.ok("重置密码成功！");
                })
                .orElse(APIResponse.badRequest("用户不存在！"));
    }
}
