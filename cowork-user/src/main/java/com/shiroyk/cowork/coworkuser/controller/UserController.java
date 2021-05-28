package com.shiroyk.cowork.coworkuser.controller;

import com.shiroyk.cowork.coworkcommon.dto.APIResponse;
import com.shiroyk.cowork.coworkcommon.dto.UserDto;
import com.shiroyk.cowork.coworkuser.dto.request.UserInfo;
import com.shiroyk.cowork.coworkuser.model.User;
import com.shiroyk.cowork.coworkuser.service.GroupService;
import com.shiroyk.cowork.coworkuser.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;
    private final GroupService groupService;

    /**
     * @Description: 获取用户信息
     * @param id 用户Id
     * @return 用户信息
     */
    @GetMapping()
    public APIResponse<UserDto> getUser(@RequestHeader("X-User-Id") String id) {
        return userService.findById(id)
                .map(user -> APIResponse.ok(user.toUserDtoL()))
                .orElse(APIResponse.badRequest("无权访问!"));
    }

    /**
     * @Description: 更新用户信息
     * @param id 用户Id
     * @param info 用户信息
     * @return 成功或失败信息
     */
    @PutMapping()
    public APIResponse<Object> updateUserInfo(@RequestHeader("X-User-Id") String id,
                                              @Valid UserInfo info) {
        return userService.findById(id).map(user -> {
            if (userService.findUserByName(info.getUsername()).isPresent()) {
                return APIResponse.badRequest("用户名已被其他用户使用!");
            }
            user.setUsername(info.getUsername());
            user.setNickname(info.getNickname());
            user.setPassword(new BCryptPasswordEncoder().encode(info.getPassword()));
            user.setEmail(info.getEmail());
            userService.save(user);
            return APIResponse.ok("更新信息成功！");
        }).orElse(APIResponse.badRequest("无权访问!"));
    }

    /**
     * @Description: 更新用户头像
     * @param id 用户Id
     * @param avatar 头像Base64
     * @return 成功或失败信息
     */
    @PostMapping("/avatar")
    public APIResponse<Object> updateUserAvatar(@RequestHeader("X-User-Id") String id,
                                              String avatar) {
        return userService.findById(id).map(user -> {
            if (avatar.length() < 102400) {
                user.setAvatar(avatar);
                userService.save(user);
                return APIResponse.ok("更新头像成功！");
            }
            return APIResponse.badRequest("头像需要小于100KB！");
        })
        .orElse(APIResponse.badRequest("无权访问!"));
    }

    /**
     * @Description: 获取用户
     * @param id 用户Id
     * @return 用户简略信息
     */
    @GetMapping("/{id}")
    public APIResponse<UserDto> findUser(@PathVariable String id) {
        return userService.findUserDtoById(id)
                .map(APIResponse::ok)
                .orElse(APIResponse.badRequest("未找到用户!"));
    }

    /**
     * @Description: 搜寻用户
     * @param name 用户名
     * @return 用户简略信息列表
     */
    @GetMapping("/search")
    public APIResponse<List<UserDto>> searchUser(@RequestParam(required = false, defaultValue = "", value = "n") String name) {
        return APIResponse.ok(
                userService.findUsersByUsernameContains(name)
                        .stream().map(User::toUserDtoS)
                        .collect(Collectors.toList()));
    }

    /**
     * @Description: 获取用户列表
     * @param idList 用户Id列表
     * @return 用户简略信息列表
     */
    @PostMapping("/list")
    public APIResponse<List<UserDto>> findUserList(@RequestBody List<String> idList) {
        return APIResponse.ok(userService.findUserByIdList(idList).map(User::toUserDtoL).collect(Collectors.toList()));
    }
}
