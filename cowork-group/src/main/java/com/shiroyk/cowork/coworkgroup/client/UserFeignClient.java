package com.shiroyk.cowork.coworkgroup.client;

import com.shiroyk.cowork.coworkcommon.dto.APIResponse;
import com.shiroyk.cowork.coworkcommon.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@FeignClient(value = "cowork-user/client")
public interface UserFeignClient {
    @GetMapping("/{id}")
    APIResponse<UserDto> getUser(@PathVariable String id);

    @PostMapping("/{group}/user")
    APIResponse<List<UserDto>> getUserList(@PathVariable String group,
                                           @RequestParam Integer page,
                                           @RequestParam Integer size);

    @PostMapping("/list")
    APIResponse<List<UserDto>> getUserList(@RequestBody Set<String> idList);

    @PostMapping("/{id}/group")
    APIResponse<?> addUserGroup(@PathVariable String id, @RequestParam String group, @RequestParam boolean force);

    @DeleteMapping("/{id}/group")
    APIResponse<?> removeUserGroup(@PathVariable String id);
}

