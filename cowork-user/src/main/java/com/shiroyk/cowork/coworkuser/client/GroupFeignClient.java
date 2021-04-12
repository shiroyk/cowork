package com.shiroyk.cowork.coworkuser.client;

import com.shiroyk.cowork.coworkcommon.dto.APIResponse;
import com.shiroyk.cowork.coworkcommon.dto.GroupDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "cowork-group/client")
public interface GroupFeignClient {
    @GetMapping("/{id}")
    APIResponse<GroupDto> getGroup(@PathVariable("id") String id);
}
