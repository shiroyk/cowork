package com.shiroyk.cowork.coworkdoc.client;

import com.shiroyk.cowork.coworkcommon.dto.APIResponse;
import com.shiroyk.cowork.coworkcommon.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@FeignClient(value = "cowork-user/client")
public interface UserFeignClient {
    @GetMapping("/count")
    APIResponse<Long> getUserSize();

    @GetMapping("/{id}")
    APIResponse<UserDto> getUser(@PathVariable String id);

    @GetMapping("/{id}/star")
    APIResponse<Set<String>> getUserDocStar(@PathVariable String id);

    @PutMapping("/{id}/star")
    APIResponse<?> putUserDocStar(@PathVariable String id, @RequestParam String docId);

    @DeleteMapping("/{id}/star")
    APIResponse<?> deleteUserDocStar(@PathVariable String id, @RequestParam String docId);

    @PutMapping("/{id}/recent")
    APIResponse<Object> updateUserRecentDoc(@PathVariable String id, @RequestParam String docId);
}