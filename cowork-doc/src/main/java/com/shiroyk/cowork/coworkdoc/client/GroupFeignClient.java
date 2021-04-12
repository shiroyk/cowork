package com.shiroyk.cowork.coworkdoc.client;

import com.shiroyk.cowork.coworkcommon.dto.APIResponse;
import com.shiroyk.cowork.coworkcommon.dto.GroupDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(value = "cowork-group/client")
public interface GroupFeignClient {
    @GetMapping("/count")
    APIResponse<Long> getGroupSize();

    @GetMapping("/{id}/users")
    APIResponse<List<String>> getUsers(@PathVariable String id);

    @GetMapping("/{id}")
    APIResponse<GroupDto> getGroup(@PathVariable String id);

    @PutMapping("/{id}")
    APIResponse<?> addGroupDoc(@PathVariable String id, @RequestParam String did);
}
