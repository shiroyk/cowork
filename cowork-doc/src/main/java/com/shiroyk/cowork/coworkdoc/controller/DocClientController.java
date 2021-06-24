package com.shiroyk.cowork.coworkdoc.controller;

import com.shiroyk.cowork.coworkcommon.constant.Permission;
import com.shiroyk.cowork.coworkcommon.dto.APIResponse;
import com.shiroyk.cowork.coworkcommon.dto.DocDto;
import com.shiroyk.cowork.coworkcommon.dto.Operation;
import com.shiroyk.cowork.coworkcommon.dto.UploadDoc;
import com.shiroyk.cowork.coworkcommon.model.doc.Doc;
import com.shiroyk.cowork.coworkcommon.model.doc.Owner;
import com.shiroyk.cowork.coworkdoc.service.DocNodeService;
import com.shiroyk.cowork.coworkdoc.service.DocService;
import com.shiroyk.cowork.coworkdoc.service.GroupService;
import com.shiroyk.cowork.coworkdoc.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/client")
public class DocClientController {
    private final DocService docService;
    private final GroupService groupService;
    private final UserService userService;
    private final DocNodeService docNodeService;

    /**
     * @Description: 获取群组的单个文档
     * @param group 群组Id
     * @param id 文档Id
     * @return 文档信息
     */
    @GetMapping("/{group}/{id}")
    public APIResponse<?> getDocDto(@PathVariable String group, @PathVariable String id) {
        return docService.findById(id).map(doc -> {
            if (doc.getOwnerId().equals(group))
                return APIResponse.ok(doc.toDocDto());
            return APIResponse.badRequest("没有获取文档的权限！");

        }).orElse(APIResponse.badRequest("文档不存在！"));
    }

    /**
     * @Description: 创建群组文档
     * @param group 群组Id
     * @param title 文档标题
     * @return 成功或失败消息
     */
    @PostMapping("/{group}")
    public APIResponse<?> createDoc(@PathVariable String group, String title) {
        Doc doc = new Doc();
        doc.setOwner(new Owner(group, Owner.OwnerEnum.Group));
        if (StringUtils.isEmpty(title))
            return APIResponse.badRequest("文档名不能为空！");
        doc.setTitle(title);
        if (docService.save(doc) != null)
            return APIResponse.ok("创建文档成功！");
        else
            return APIResponse.badRequest("创建文档失败！");
    }

    /**
     * @Description: 获取群组文档不在回收站的数量
     * @param group 群组Id
     * @return Long
     */
    @GetMapping("/{group}/count")
    public APIResponse<Long> countAllDoc(@PathVariable String group) {
        return APIResponse.ok(docService.countDocsByDeleteIsFalse(group));
    }

    /**
     * @Description: 获取群组不在回收站的的文档
     * @param group 群组Id
     * @param page 分页
     * @param size 数量
     * @return List<DocDto>
     */
    @GetMapping("/{group}/all")
    public APIResponse<List<DocDto>> getAllDoc(@PathVariable String group, Integer page, Integer size) {
        return APIResponse.ok(docService.findDocsByDeleteIsFalse(group, PageRequest.of(page, size))
                .stream().map(Doc::toDocDto).collect(Collectors.toList()));
    }

    /**
     * @Description: 获取群组回收站的文档数量
     * @param group 群组Id
     * @return Long
     */
    @GetMapping("/{group}/trash/count")
    public APIResponse<Long> countTrashDoc(@PathVariable String group) {
        return APIResponse.ok(docService.countDocsByDeleteIsTrue(group));
    }

    /**
     * @Description: 获取群组回收站的文档
     * @param group 群组Id
     * @param page 分页
     * @param size 数量
     * @return List<DocDto>
     */
    @GetMapping("/{group}/trash")
    public APIResponse<List<DocDto>> getAllTrash(@PathVariable String group, Integer page, Integer size) {
        return APIResponse.ok(docService.findDocsByDeleteIsTrue(group, PageRequest.of(page, size))
                .stream().map(Doc::toDocDto).collect(Collectors.toList()));
    }

    /**
     * @Description: 搜索不在回收站的群组文档
     * @param group 群组Id
     * @param title 文档标题
     * @return List<DocDto>
     */
    @GetMapping("/{group}/search")
    public APIResponse<List<DocDto>> searchDoc(@PathVariable String group,
                                               String title) {
        return APIResponse.ok(docService.searchDeleteFalse(title, group)
                .stream().map(Doc::toDocDto).collect(Collectors.toList()));
    }

    /**
     * @Description: 搜索在回收站的群组文档
     * @param group 群组Id
     * @param title 文档标题
     * @return List<DocDto>
     */
    @GetMapping("/{group}/trash/search")
    public APIResponse<List<DocDto>> searchTrashDoc(@PathVariable String group,
                                                    String title) {
        return APIResponse.ok(docService.searchDeleteTrue(title, group)
                .stream().map(Doc::toDocDto).collect(Collectors.toList()));
    }

    /**
     * @Description: 获取文档内容
     * @param group 群组Id
     * @param did 文档Id
     * @return 文档内容数据
     */
    @GetMapping("/{group}/{did}/content")
    public APIResponse<?> getDocContent(@PathVariable String group, @PathVariable String did) {
        return docService.findById(did)
                .map(doc -> {
                    if (doc.getOwnerId().equals(group))
                        return APIResponse.ok(docNodeService.getDocContent(false, did, 30));
                    return APIResponse.badRequest("没有获取文档的权限！");
                })
                .orElse(APIResponse.badRequest("文档不存在！"));
    }

    /**
     * @Description: 群组文档标记已删除
     * @param group 群组Id
     * @param did 文档Id
     * @return 成功或失败消息
     */
    @DeleteMapping("/{group}/{did}")
    public APIResponse<?> trashDoc(@PathVariable String group, @PathVariable String did) {
        return docService.findById(did)
                .map(doc -> {
                    if (doc.getOwnerId().equals(group)) {
                        doc.setDelete(true);
                        docService.save(doc);
                        return APIResponse.ok("文档放入回收站成功！");
                    }
                    return APIResponse.badRequest("没有获取文档的权限！");
                }).orElse(APIResponse.badRequest("文档不存在！"));
    }

    /**
     * @Description: 恢复标记已删除的文档
     * @param group 群组Id
     * @param did 文档Id
     * @return 成功或失败消息
     */
    @PutMapping("/{group}/trash/{did}")
    public APIResponse<?> recoveryDoc(@PathVariable String group, @PathVariable String did) {
        return docService.findById(did)
                .map(doc -> {
                    if (doc.getOwnerId().equals(group)) {
                        doc.setDelete(false);
                        docService.save(doc);
                        return APIResponse.ok("恢复文档成功！");
                    }
                    return APIResponse.badRequest("没有获取文档的权限！");
                }).orElse(APIResponse.badRequest("文档不存在！"));
    }

    /**
     * @Description: 彻底删除群组回收站的文档
     * @param group 群组Id
     * @param did 文档Id
     * @return 成功或失败消息
     */
    @DeleteMapping("/{group}/trash/{did}")
    public APIResponse<?> deleteDoc(@PathVariable String group, @PathVariable String did) {
        return docService.findById(did)
                .map(doc -> {
                    if (!doc.isDelete())
                        return APIResponse.ok("文档不在回收站中！");
                    if (doc.getOwnerId().equals(group)) {
                        docService.delete(doc.getId());
                        docNodeService.deleteAll(did); // 异步删除全部的文档对象
                        return APIResponse.ok("删除文档成功！");
                    }
                    return APIResponse.badRequest("没有删除文档的权限！");
                }).orElse(APIResponse.badRequest("文档不存在！"));
    }

    /**
     * @Description: 获取用户是否有访问文档的权限
     * @param uid 用户Id
     * @param did 文档Id
     * @return 权限信息
     */
    @PostMapping("/permission")
    public APIResponse<Permission> hasPermission(String uid, String did) {
        return docService.findById(did).map(doc -> {
            try {
                // 更新用户最近编辑文档
                userService.addUserRecentDoc(uid, did);
            } catch (Exception ignore) { }
            // 文档属于用户自己或具有公开的URL
            if (doc.belongUser()) {
                if (uid.equals(doc.getOwnerId()))
                    return APIResponse.ok(Permission.ReadWrite);
                else
                    return APIResponse.ok(doc.getUrlPermission());
            } else {
                // 文档属于群组，只有群组内部成员可以访问
                if (groupService.existUser(doc.getOwnerId(), uid))
                    return APIResponse.ok(Permission.ReadWrite);
            }
            return APIResponse.ok(Permission.Empty);
        }).orElse(APIResponse.ok(Permission.Empty));
    }

    /**
     * @Description: 上传文档
     * @param group 群组ID
     * @param uid 用户ID, 用于生成操作对象
     * @param uploadDoc 上传的文档对象
     * @return 成功或失败信息
     */
    @PostMapping("/{group}/upload")
    public APIResponse<?> uploadDoc(@RequestHeader("X-User-Id") String uid, @PathVariable String group, @RequestBody UploadDoc uploadDoc) {
        Doc doc = docService.save(Doc.createGroupDoc(uploadDoc.getDocName(), group));

        if (doc == null) return APIResponse.badRequest("上传文档失败，请稍后重试！");

        if (uploadDoc.getCrdts() == null || uploadDoc.getCrdts().length == 0)
            return APIResponse.ok("上传文档成功，共解析到0个字符！");

        Operation op = new Operation(uid, doc.getId(), uploadDoc.getCrdts());
        docNodeService.saveUploadDoc(op);

        return APIResponse.ok("上传文档成功！");
    }

}
