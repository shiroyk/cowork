package com.shiroyk.cowork.coworkuser.controller;

import com.shiroyk.cowork.coworkcommon.constant.Role;
import com.shiroyk.cowork.coworkcommon.dto.APIResponse;
import com.shiroyk.cowork.coworkuser.model.User;
import com.shiroyk.cowork.coworkuser.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/user")
@Slf4j
@AllArgsConstructor
public class UserAdminController {
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
     * @Description: 获取用户
     * @param page 分页
     * @param size 数量
     * @return List<User>
     */
    @GetMapping("")
    public APIResponse<List<User>> getUser(@RequestParam(required = false, defaultValue = "0", value = "p") Integer page,
                                           @RequestParam(required = false, defaultValue = "10", value = "s") Integer size) {
        return APIResponse.ok(userService.findAllUser(PageRequest.of(page, size)).toList());
    }

    /**
     * @Description: 创建用户
     * @param username 用户名
     * @param password 密码
     * @param email 邮箱
     * @param role 权限
     * @return 成功或失败信息
     */
    @PostMapping("")
    public APIResponse<?> createUser(String username,
                                          String password,
                                          String email,
                                          Integer role) {
        return userService.findUserByName(username)
                .map(user -> APIResponse.badRequest("用户名已存在！"))
                .orElseGet(() -> {
                    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
                    User user = new User(username, email, encoder.encode(password), Role.valueOf(role));
                    if (!StringUtils.isEmpty(userService.save(user).getId()))
                        return APIResponse.ok("新建用户成功！");
                    else
                        return APIResponse.badRequest("新建用户失败！");
                });
    }

    /**
     * @Description: 获取单个用户
     * @param id 用户Id
     * @return 用户信息
     */
    @GetMapping("/{id}")
    public APIResponse<User> getUser(@PathVariable String id) {
        return userService.findById(id)
                .map(APIResponse::ok)
                .orElse(APIResponse.ok("用户不存在！"));
    }

    /**
     * @Description: 更新用户信息
     * @param id 用户Id
     * @param username 用户名
     * @param nickname 用户昵称
     * @param password 密码
     * @param email 邮箱
     * @param role 权限
     * @return 成功或失败信息
     */
    @PutMapping("/{id}")
    public APIResponse<?> updateUser(@PathVariable String id,
                                          String username,
                                          String nickname,
                                          String password,
                                          String email,
                                          String role) {
        return userService.findById(id)
                .map(user -> {
                    if (!StringUtils.isEmpty(username))
                        user.setUsername(username);
                    user.setNickname(nickname);
                    user.setEmail(email);
                    user.setRole(Role.valueOf(role));
                    if (!StringUtils.isEmpty(password))
                        user.setPassword(new BCryptPasswordEncoder().encode(password));
                    userService.save(user);
                    return APIResponse.ok("更新用户成功！");
                })
                .orElse(APIResponse.badRequest("用户不存在！"));
    }

    /**
     * @Description: 删除用户
     * @param id 用户Id
     * @return 成功或失败信息
     */
    @DeleteMapping("/{id}")
    public APIResponse<?> deleteUser(@PathVariable String id) {
        return userService.findById(id)
                .map(user -> {
                    user.setIsEnable();
                    userService.save(user);
                    String msg = user.getIsEnable() ? "取消" : "";
                    return APIResponse.ok(msg + "禁用成功！");
                })
                .orElse(APIResponse.ok("用户不存在！"));
    }
}
