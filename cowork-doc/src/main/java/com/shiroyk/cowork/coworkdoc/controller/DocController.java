package com.shiroyk.cowork.coworkdoc.controller;

import com.shiroyk.cowork.coworkcommon.constant.Permission;
import com.shiroyk.cowork.coworkcommon.dto.APIResponse;
import com.shiroyk.cowork.coworkcommon.dto.DocDto;
import com.shiroyk.cowork.coworkcommon.dto.Operation;
import com.shiroyk.cowork.coworkcommon.dto.UploadDoc;
import com.shiroyk.cowork.coworkdoc.model.Doc;
import com.shiroyk.cowork.coworkdoc.service.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/doc")
public class DocController {
    private final DocService docService;
    private final DocNodeService docNodeService;
    private final GroupService groupService;
    private final UserService userService;
    private final FileService fileService;

    /**
     * @Description: 获取未被标记为已删除的文档数量
     * @param uid 用户Id
     * @return Long
     */
    @GetMapping("/count")
    public APIResponse<Long> countAllDoc(@RequestHeader("X-User-Id") String uid) {
        return APIResponse.ok(docService.countDocsByDeleteIsFalse(uid));
    }

    /**
     * @Description: 获取未被标记为已删除的文档
     * @param uid 用户Id
     * @param page 分页
     * @param size 数量
     * @return List<DocDto>
     */
    @GetMapping()
    public APIResponse<List<DocDto>> getAllDoc(@RequestHeader("X-User-Id") String uid,
                                               @RequestParam(required = false, defaultValue = "0", value = "p") Integer page,
                                               @RequestParam(required = false, defaultValue = "10", value = "s") Integer size) {
        return APIResponse.ok(docService.findDocsByDeleteIsFalse(uid, PageRequest.of(page, size))
                .stream().map(Doc::toDocDto).collect(Collectors.toList()));
    }

    /**
     * @Description: 创建文档
     * @param uid 用户Id
     * @param title 文档标题
     * @return 成功或失败信息
     */
    @PostMapping()
    public APIResponse<?> createDoc(@RequestHeader("X-User-Id") String uid, String title) {
        Doc doc = new Doc();
        doc.setOwner(new DocDto.Owner(uid, DocDto.OwnerEnum.User));
        if (StringUtils.isEmpty(title))
            return APIResponse.badRequest("文档名不能为空！");
        doc.setTitle(title);
        if (docService.save(doc) != null)
            return APIResponse.ok("创建文档成功！");
        else
            return APIResponse.badRequest("创建文档失败！");
    }

    /**
     * @Description: 更新文档信息
     * @param uid 用户Id
     * @param did 文档Id
     * @param title 文档标题
     * @param delete 是否删除
     * @return 成功或失败信息
     */
    @PutMapping("/{did}")
    public APIResponse<?> updateDoc(@RequestHeader("X-User-Id") String uid,
                                    @PathVariable String did,
                                    String title,
                                    Boolean delete) {
        return docService.findById(did)
                .map(doc -> {
                    if (uid.equals(doc.getOwnerId())) {
                        if (!StringUtils.isEmpty(title))
                            doc.setTitle(title);
                        if (delete != null)
                            doc.setDelete(delete);
                        docService.save(doc);
                        return APIResponse.ok("更新文档成功");
                    } else
                        return APIResponse.badRequest("没有获取该文档的权限！");
                }).orElse(APIResponse.badRequest("文档不存在！"));
    }

    /**
     * @Description: 更新文档链接
     * @param uid 用户Id
     * @param did 文档Id
     * @param permission 链接权限
     * @return 文档新链接
     */
    @PutMapping("/{did}/url")
    public APIResponse<?> updateDocUrl(@RequestHeader("X-User-Id") String uid, @PathVariable String did, Permission permission) {
        return docService.findById(did)
                .map(doc -> {
                    if (uid.equals(doc.getOwnerId()))
                        doc.createUrl(permission);
                    else if (doc.belongGroup()) {
                        // 文档属于群组且用户在这个群组里
                        if (groupService.existUser(doc.getOwnerId(), uid))
                            doc.createUrl(Permission.ReadWrite);
                    }
                    else
                        return APIResponse.badRequest("没有获取该文档的权限！");
                    docService.save(doc);
                    return APIResponse.ok(doc.toDocDto());
                }).orElse(APIResponse.badRequest("文档不存在！"));
    }

    /**
     * @Description: 获取单个文档
     * @param uid 用户Id
     * @param did 文档Id
     * @return 文档信息
     */
    @GetMapping("/{did}")
    public APIResponse<?> getDoc(@RequestHeader("X-User-Id") String uid, @PathVariable String did) {
        return docService.findById(did)
                .map(doc -> {
                    if (doc.belongUser()) {
                        if (uid.equals(doc.getOwnerId()))
                            return APIResponse.ok(doc.toDocDto(true));
                    } else {
                        if (groupService.existUser(doc.getOwnerId(), uid))
                            return APIResponse.ok(doc.toDocDto(true));
                    }
                    return APIResponse.badRequest("没有获取该文档的权限！");
                }).orElse(APIResponse.badRequest("文档不存在！"));
    }

    /**
     * @Description: 根据文档链接获取文档信息
     * @param uid 用户Id
     * @param url 文档链接
     * @return 文档信息
     */
    @GetMapping("/url/{url}")
    public APIResponse<?> getDocByUrl(@RequestHeader("X-User-Id") String uid, @PathVariable String url) {
        return docService.findDocByUrl(url)
                .map(doc -> {
                    if (doc.belongUser()) {
                        // 公开的URL具有读写权限
                        if (doc.hasPermission())
                            return APIResponse.ok(doc.toDocDto(false));
                    } else {
                        // 属于群组且用户在群组里
                        if (groupService.existUser(doc.getOwnerId(), uid))
                            return APIResponse.ok(doc.toDocDto(true));
                    }
                    return APIResponse.badRequest("没有获取该文档的权限！");
                }).orElse(APIResponse.badRequest("文档不存在！"));
    }

    /**
     * @Description: 获取文档内容
     * @param uid 用户Id
     * @param did 文档Id
     * @param tombstone 标记为已删除的字符数量
     * @return 文档内容数据
     */
    @GetMapping("/{did}/content")
    public APIResponse<?> getDocContent(@RequestHeader("X-User-Id") String uid,
                                        @PathVariable String did,
                                        @RequestParam(required = false, defaultValue = "30", value = "tombstone") Integer tombstone) {
        return docService.findById(did)
                .map(doc -> {
                    if (doc.belongUser()) {
                        // 文档属于用户自己或公开的URL具有读写权限
                        if (uid.equals(doc.getOwnerId()) || doc.hasPermission())
                            return APIResponse.ok(docNodeService.getDocContent(did, tombstone));
                    } else {
                        // 文档属于群组，只有群组内部成员可以获取
                        if (groupService.existUser(doc.getOwnerId(), uid))
                            return APIResponse.ok(docNodeService.getDocContent(did, tombstone));
                    }
                    return APIResponse.badRequest("没有获取该文档的权限！");
                }).orElse(APIResponse.badRequest("文档不存在！"));
    }

    /**
     * @Description: 标记文档为删除
     * @param uid 用户Id
     * @param did 文档Id
     * @return 成功或失败信息
     */
    @DeleteMapping("/{did}")
    public APIResponse<?> deleteDoc(@RequestHeader("X-User-Id") String uid, @PathVariable String did) {
        return docService.findById(did)
                .map(doc -> {
                    if (doc.belongUser() && uid.equals(doc.getOwnerId())) {
                        doc.setDelete(true);
                        docService.save(doc);
                        userService.deleteUserDocStar(uid, did); // 从用户收藏中删除文档
                        return APIResponse.ok("文档放入回收站成功！");
                    }
                    return APIResponse.badRequest("没有获取该文档的权限！");
                }).orElse(APIResponse.badRequest("文档不存在！"));
    }

    /**
     * @Description: 获取收藏的文档
     * @param uid 用户Id
     * @return List<DocDto>
     */
    @GetMapping("/star")
    public APIResponse<List<DocDto>> getStar(@RequestHeader("X-User-Id") String uid) {
        return APIResponse.ok(docService.findAllById(userService.getUserDocStar(uid))
                        .map(Doc::toDocDto).collect(Collectors.toList()));
    }

    /**
     * @Description: 更新收藏状态，每次请求取反收藏状态
     * @param uid 用户Id
     * @param did 文档Id
     * @return 成功或失败信息
     */
    @PutMapping("/{did}/star")
    public APIResponse<?> putUserDocStar(@RequestHeader("X-User-Id") String uid, @PathVariable String did) {
        return userService.putUserDocStar(uid, did);
    }

    /**
     * @Description: 搜索标记为未删除的文档
     * @param uid 用户Id
     * @param title 文档标题
     * @return List<DocDto>
     */
    @GetMapping("/search")
    public APIResponse<List<DocDto>> searchDoc(@RequestHeader("X-User-Id") String uid,
                                               @RequestParam(required = false, defaultValue = "", value = "title") String title) {
        return APIResponse.ok(docService.searchDeleteFalse(title, uid)
                .stream().map(Doc::toDocDto).collect(Collectors.toList()));
    }

    /**
     * @Description: 获取文档标记为已删除文档的数量
     * @param uid 用户Id
     * @return Long
     */
    @GetMapping("/trash/count")
    public APIResponse<Long> countAllTrashDoc(@RequestHeader("X-User-Id") String uid) {
        return APIResponse.ok(docService.countDocsByDeleteIsTrue(uid));
    }

    /**
     * @Description: 获取文档标记为已删除文档
     * @param uid 用户Id
     * @param page 分页
     * @param size 数量
     * @return List<DocDto>
     */
    @GetMapping("/trash")
    public APIResponse<List<DocDto>> getTrash(@RequestHeader("X-User-Id") String uid,
                                              @RequestParam(required = false, defaultValue = "0", value = "p") Integer page,
                                              @RequestParam(required = false, defaultValue = "10", value = "s") Integer size) {
        return APIResponse.ok(docService.findDocsByDeleteIsTrue(uid, PageRequest.of(page, size))
                .stream().map(Doc::toDocDto).collect(Collectors.toList()));
    }

    /**
     * @Description: 彻底删除文档
     * @param uid 用户Id
     * @param did 文档Id
     * @return 成功或失败信息
     */
    @DeleteMapping("/trash/{did}")
    public APIResponse<?> deleteTrashDoc(@RequestHeader("X-User-Id") String uid,
                                         @PathVariable String did) {
        return docService.findById(did)
                .map(doc -> {
                    if (doc.belongUser() && uid.equals(doc.getOwnerId())) {
                        if (doc.isDelete()) {
                            docService.delete(did);
                            userService.deleteUserDocStar(uid, did); // 从用户收藏中删除文档
                            docNodeService.deleteAll(did); // 异步删除全部的文档对象
                            return APIResponse.ok("彻底删除文档成功！");
                        }
                        return APIResponse.badRequest("文档不在回收站！");
                    }
                    return APIResponse.badRequest("没有获取该文档的权限!");
                }).orElse(APIResponse.badRequest("文档不存在！"));
    }

    /**
     * @Description: 恢复标记为已删除的文档
     * @param uid 用户Id
     * @param did 文档Id
     * @return 成功或失败信息
     */
    @PutMapping("/trash/{did}")
    public APIResponse<?> recoveryDoc(@RequestHeader("X-User-Id") String uid,
                                                 @PathVariable String did) {
        return docService.findById(did)
                .map(doc -> {
                    if (doc.belongUser() && uid.equals(doc.getOwnerId())) {
                        if (doc.isDelete()) {
                            doc.setDelete(false);
                            docService.save(doc);
                            return APIResponse.ok("恢复文档成功！");
                        }
                        return APIResponse.badRequest("文档不在回收站！");
                    }
                    return APIResponse.badRequest("没有获取该文档的权限!");
                }).orElse(APIResponse.badRequest("文档不存在！"));
    }

    /**
     * @Description: 搜索标记为已删除的文档
     * @param uid 用户Id
     * @param title 文档名
     * @return List<DocDto>
     */
    @GetMapping("/trash/search")
    public APIResponse<List<DocDto>> searchTrashDoc(@RequestHeader("X-User-Id") String uid,
                                                    @RequestParam(required = false, defaultValue = "", value = "title") String title) {
        return APIResponse.ok(docService.searchDeleteTrue(title, uid)
                .stream().map(Doc::toDocDto).collect(Collectors.toList()));
    }

    /**
     * @Description: 获取文档内容中的图片
     * @param fileName 图片名
     * @param request
     * @return 图片资源
     */
    @GetMapping("/image/{fileName:.+}")
    public ResponseEntity<Resource> downloadImage(@PathVariable String fileName, HttpServletRequest request) {
        try {
            Resource resource = fileService.loadImageAsResource(fileName);

            if (resource == null) {
               return ResponseEntity.status(404).build();
            }
            String contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            if(contentType == null) {
                contentType = "application/octet-stream";
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (IOException ex) {
            ex.printStackTrace();
            return ResponseEntity.status(404).build();
        }
    }

    /**
     * @Description: 上传文档图片
     * @param file 图片文件
     * @return 返回图片链接
     */
    @PostMapping("/image")
    public APIResponse<?> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty() || file.getSize() == 0) {
            return APIResponse.badRequest("文件为空！");
        }

        String fileName = fileService.storeImage(file);
        if (StringUtils.isEmpty(fileName)) {
            return APIResponse.badRequest("上传失败，请检查文件！");
        }
        return APIResponse.ok("doc/image/" + fileName);
    }

    /**
     * @Description: 上传文档
     * @param uid 用户ID
     * @param uploadDoc 上传的文档对象
     * @return 成功或失败信息
     */
    @PostMapping("/uploadDoc")
    public APIResponse<?> uploadDoc(@RequestHeader("X-User-Id") String uid, @RequestBody UploadDoc uploadDoc) {
        Doc doc = docService.save(Doc.createUserDoc(uploadDoc.getDocName(), uid));

        if (doc == null) return APIResponse.badRequest("上传文档失败，请稍后重试！");

        if (uploadDoc.getCrdts() == null || uploadDoc.getCrdts().length == 0) {
            return APIResponse.ok("上传文档成功，共解析到0个字符！");
        }

        Operation op = new Operation(uid, doc.getId(), uploadDoc.getCrdts());
        docNodeService.saveUploadDoc(op);

        return APIResponse.ok("上传文档成功！");
    }

}
