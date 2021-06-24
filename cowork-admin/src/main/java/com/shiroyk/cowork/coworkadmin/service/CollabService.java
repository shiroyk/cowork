package com.shiroyk.cowork.coworkadmin.service;

import com.shiroyk.cowork.coworkadmin.client.CollabFeignClient;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CollabService {
    private final CollabFeignClient collabFeignClient;

    public Long getOnlineUser() {
        return collabFeignClient.getOnlineUser();
    }
}
