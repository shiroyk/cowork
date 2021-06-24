package com.shiroyk.cowork.coworkadmin.controller;

import com.shiroyk.cowork.coworkadmin.dto.CreateUser;
import com.shiroyk.cowork.coworkadmin.dto.UpdateUser;
import com.shiroyk.cowork.coworkadmin.service.UserService;
import com.shiroyk.cowork.coworkcommon.constant.Role;
import com.shiroyk.cowork.coworkcommon.dto.APIResponse;
import com.shiroyk.cowork.coworkcommon.model.user.User;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/admin/user")
@Slf4j
@AllArgsConstructor
public class UserController {
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
    @GetMapping()
    public APIResponse<List<User>> getUser(@RequestParam(required = false, defaultValue = "0", value = "p") Integer page,
                                           @RequestParam(required = false, defaultValue = "10", value = "s") Integer size) {
        return APIResponse.ok(userService.findAllUser(PageRequest.of(page, size)).toList());
    }

    /**
     * @Description: 创建用户
     * @param createUser 用户信息
     * @return 成功或失败信息
     */
    @PostMapping()
    public APIResponse<?> createUser(@Valid CreateUser createUser) {
        return userService.findUserByName(createUser.getUsername())
                .map(user -> APIResponse.badRequest("用户名已存在！"))
                .orElseGet(() -> {
                    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
                    User user = new User(
                            createUser.getUsername(),
                            createUser.getEmail(),
                            encoder.encode(createUser.getPassword()),
                            Role.valueOf(createUser.getRole()));
                    userService.save(user);
                    return APIResponse.ok("新建用户成功！");
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
     * @param updateUser 用户信息
     * @return 成功或失败信息
     */
    @PutMapping("/{id}")
    public APIResponse<?> updateUser(@PathVariable String id,
                                     @Valid UpdateUser updateUser) {
        return userService.findById(id)
                .map(user -> {
                    user.setUsername(updateUser.getUsername());
                    user.setNickname(updateUser.getNickname());
                    user.setEmail(updateUser.getEmail());
                    user.setRole(Role.valueOf(updateUser.getRole()));
                    user.setPassword(new BCryptPasswordEncoder().encode(updateUser.getPassword()));
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
