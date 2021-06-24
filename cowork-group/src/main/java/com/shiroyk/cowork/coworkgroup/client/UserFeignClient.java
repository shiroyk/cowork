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

    @PostMapping("/detailList")
    APIResponse<List<UserDto>> getUserDetailList(@RequestBody List<String> idList);

    @PostMapping("/list")
    APIResponse<List<UserDto>> getUserList(@RequestBody Set<String> idList);

    @PostMapping("/{uid}/group")
    APIResponse<?> setUserGroup(@PathVariable String uid, @RequestParam String group);

    @DeleteMapping("/{uid}/group")
    APIResponse<?> removeUserGroup(@PathVariable String uid, @RequestParam String group);
}

