package com.shiroyk.cowork.coworkuser.controller;

import com.shiroyk.cowork.coworkcommon.constant.Role;
import com.shiroyk.cowork.coworkcommon.dto.APIResponse;
import com.shiroyk.cowork.coworkcommon.dto.UserDto;
import com.shiroyk.cowork.coworkuser.model.User;
import com.shiroyk.cowork.coworkuser.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

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
     * @Description: 获取群组的用户列表
     * @param group 群组Id
     * @param page 分页
     * @param size 数量
     * @return 用户信息
     */
    @PostMapping("/{group}/user")
    public APIResponse<List<UserDto>> getUserList(@PathVariable String group, Integer page, Integer size) {
        return APIResponse.ok(userService.findUsersByGroup(group, PageRequest.of(page, size)));
    }

    /**
     * @Description: 获取用户列表
     * @param idList 用户Id列表
     * @return 用户信息列表
     */
    @PostMapping("/list")
    public APIResponse<List<UserDto>> getUserList(@RequestBody List<String> idList) {
        return APIResponse.ok(userService.findUserDtoListById(idList));
    }

    /**
     * @Description: 设置用户群组
     * @param id 用户Id
     * @param group 群组Id
     * @param force 是否强制加入，目前未做用户可加入多个群组
     * @return 成功或失败信息
     */
    @PostMapping("/{id}/group")
    public APIResponse<?> setUserGroup(@PathVariable String id, String group, boolean force) {
        return userService.findById(id)
                .map(user -> {
                    if (force || StringUtils.isEmpty(user.getGroup())) {
                        user.setGroup(group);
                        userService.save(user);
                        return APIResponse.ok("用户加入群组成功！");
                    }
                    if (group.equals(user.getGroup()))
                        return APIResponse.badRequest("用户已加入该群组！");
                    return APIResponse.badRequest("用户已加入其他群组！");
                })
                .orElse(APIResponse.badRequest("用户不存在！"));
    }

    /**
     * @Description: 将用户从群组移除
     * @param id 用户Id
     * @return 成功或失败信息
     */
    @DeleteMapping("/{id}/group")
    public APIResponse<?> removeUserGroup(@PathVariable String id) {
        return userService.findById(id)
                .map(user -> {
                    if (StringUtils.isEmpty(user.getGroup()))
                        return APIResponse.badRequest("用户没有加入群组！");
                    else
                        user.setGroup(null);
                    userService.save(user);
                    return APIResponse.ok("移出群组成功！");
                })
                .orElse(APIResponse.badRequest("用户不存在！"));
    }

    /**
     * @Description: 添加用户最近访问的文档
     * @param id 用户Id
     * @param docId 文档Id
     */
    @PutMapping("/{id}/recent")
    public APIResponse<?> updateUserRecentDoc(@PathVariable String id,
                                          String docId) {
        return userService.findById(id)
                .map(user -> {
                    List<String> recent = user.getRecent();
                    if (recent.size() >= 10) {
                        recent.remove(10);
                    }
                    if (!recent.contains(docId)) {
                        recent.add(docId);
                    }
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
