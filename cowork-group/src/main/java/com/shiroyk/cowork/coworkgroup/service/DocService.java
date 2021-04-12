package com.shiroyk.cowork.coworkgroup.service;

import com.shiroyk.cowork.coworkcommon.constant.ResultCode;
import com.shiroyk.cowork.coworkcommon.dto.APIResponse;
import com.shiroyk.cowork.coworkcommon.dto.DocDto;
import com.shiroyk.cowork.coworkgroup.client.DocFeignClient;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class DocService {
    private final DocFeignClient docFeignClient;

    public APIResponse<DocDto> getDocDto(String group, String id) {
        return docFeignClient.getDocDto(group, id);
    }

    public APIResponse<?> createDoc(String group, String title) {
        return docFeignClient.createDoc(group, title);
    }

    public ResultCode updateDocOwner(String group, String did, String owner) {
        return docFeignClient.updateDocOwner(group, did, owner).getCode();
    }

    public APIResponse<Long> countAllDoc(String group) {
        return docFeignClient.countAllDoc(group);
    }

    public APIResponse<List<DocDto>> getAllDoc(String group, Integer page, Integer size) {
        return docFeignClient.getAllDoc(group, page, size);
    }

    public APIResponse<Long> countTrashDoc(String group) {
        return docFeignClient.countTrashDoc(group);
    }

    public APIResponse<List<DocDto>> getAllTrash(String group, Integer page, Integer size) {
        return docFeignClient.getAllTrash(group, page, size);
    }

    public APIResponse<List<DocDto>> searchDoc(String group, String title) {
        return docFeignClient.searchDoc(group, title);
    }

    public APIResponse<List<DocDto>> searchTrashDoc(String group, String title) {
        return docFeignClient.searchTrashDoc(group, title);
    }

    public APIResponse<?> getDocContent(String group, String did) {
        return docFeignClient.getDocContent(group, did);
    }

    public APIResponse<?> trashDoc(String group, String did) {
        return docFeignClient.trashDoc(group, did);
    }

    public APIResponse<?> recoveryDoc(String group, String did) {
        return docFeignClient.recoveryDoc(group, did);
    }

    public APIResponse<?> deleteDoc(String group, String did) {
        return docFeignClient.deleteDoc(group, did);
    }
}
