package com.shiroyk.cowork.coworkcollab.service;

import com.shiroyk.cowork.coworkcommon.constant.Permission;
import com.shiroyk.cowork.coworkcommon.constant.ResultCode;
import com.shiroyk.cowork.coworkcommon.dto.APIResponse;
import com.shiroyk.cowork.coworkcollab.client.DocFeignClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@AllArgsConstructor
public class DocService {
    private final DocFeignClient docFeignClient;

    public Permission getPermission(String uid, String did) {
        return docFeignClient.getPermission(uid, did).getData();
    }
}
