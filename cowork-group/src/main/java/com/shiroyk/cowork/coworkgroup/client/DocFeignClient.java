package com.shiroyk.cowork.coworkgroup.client;

import com.shiroyk.cowork.coworkcommon.dto.APIResponse;
import com.shiroyk.cowork.coworkcommon.dto.DocDto;
import com.shiroyk.cowork.coworkcommon.dto.UploadDoc;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(value = "cowork-doc/client")
public interface DocFeignClient {
    @GetMapping("/{group}/{id}")
    APIResponse<DocDto> getDocDto(@PathVariable String group, @PathVariable String id);

    @PostMapping("/{group}")
    APIResponse<?> createDoc(@PathVariable String group, @RequestParam String title);

    @PutMapping("/{group}/{did}")
    APIResponse<?> updateDocOwner(@PathVariable String group, @PathVariable String did, @RequestParam String owner);

    @GetMapping("/{group}/count")
    APIResponse<Long> countAllDoc(@PathVariable String group);

    @GetMapping("/{group}/all")
    APIResponse<List<DocDto>> getAllDoc(@PathVariable String group,
                                        @RequestParam Integer page,
                                        @RequestParam Integer size);

    @GetMapping("/{group}/trash/count")
    APIResponse<Long> countTrashDoc(@PathVariable String group);

    @GetMapping("/{group}/trash")
    APIResponse<List<DocDto>> getAllTrash(@PathVariable String group,
                                          @RequestParam Integer page,
                                          @RequestParam Integer size);

    @GetMapping("/{group}/search")
    APIResponse<List<DocDto>> searchDoc(@PathVariable String group, @RequestParam String title);

    @GetMapping("/{group}/trash/search")
    APIResponse<List<DocDto>> searchTrashDoc(@PathVariable String group, @RequestParam String title);

    @GetMapping("/{group}/{id}/content")
    APIResponse<?> getDocContent(@PathVariable String group, @PathVariable String id);

    @DeleteMapping("/{group}/{did}")
    APIResponse<?> trashDoc(@PathVariable String group, @PathVariable String did);

    @PutMapping("/{group}/trash/{did}")
    APIResponse<?> recoveryDoc(@PathVariable String group, @PathVariable String did);

    @DeleteMapping("/{group}/trash/{did}")
    APIResponse<?> deleteDoc(@PathVariable String group, @PathVariable String did);

    @PostMapping("/{group}/upload")
    APIResponse<?> uploadDoc(@RequestHeader("X-User-Id") String uid,
                             @PathVariable String group,
                             @RequestBody UploadDoc uploadDoc);
}
