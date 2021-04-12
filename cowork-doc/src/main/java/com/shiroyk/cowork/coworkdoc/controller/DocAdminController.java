package com.shiroyk.cowork.coworkdoc.controller;

import com.shiroyk.cowork.coworkcommon.constant.Permission;
import com.shiroyk.cowork.coworkcommon.dto.APIResponse;
import com.shiroyk.cowork.coworkcommon.dto.DocDto;
import com.shiroyk.cowork.coworkcommon.dto.Statistic;
import com.shiroyk.cowork.coworkcommon.dto.UserDto;
import com.shiroyk.cowork.coworkdoc.model.Doc;
import com.shiroyk.cowork.coworkdoc.service.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/admin/doc")
public class DocAdminController {
    private final DocService docService;
    private final GroupService groupService;
    private final UserService userService;
    private final DocNodeService docNodeService;
    private final CollabService collabService;

    /**
     * @Description: 文档数量
     * @return Long
     */
    @GetMapping("/count")
    public APIResponse<Long> getDocSize() {
        return APIResponse.ok(docService.count());
    }

    /**
     * @Description: 请求文档数据
     * @param page 分页
     * @param size 数量
     * @return List<DocDto>
     */
    @GetMapping()
    public APIResponse<List<DocDto>> getAllDoc(@RequestParam(required = false, defaultValue = "0", value = "p") Integer page,
                                               @RequestParam(required = false, defaultValue = "10", value = "s") Integer size) {
        return APIResponse.ok(docService.findAll(PageRequest.of(page, size)).map(Doc::toDocDto).toList());
    }

    /**
     * @Description: 获取单个文档
     * @param id 文档Id
     * @return DocDto
     */
    @GetMapping("/{id}")
    public APIResponse<DocDto> getDoc(@PathVariable String id) {
        return docService.findById(id)
                .map(doc -> {
                    if (doc.belongUser()) {
                        UserDto user = userService.getUser(doc.getOwnerId());
                        return APIResponse.ok(doc.toDocDto(user));
                    } else {
                        return APIResponse.ok(doc.toDocDto(groupService.getGroup(doc.getOwnerId())));
                    }
                })
                .orElse(APIResponse.badRequest("文档不存在！"));
    }

    /**
     * @Description: 获取文档内容
     * @param did 文档Id
     * @return 文档内容数据
     */
    @GetMapping("/{did}/content")
    public APIResponse<?> getDocContent(@PathVariable String did) {
        return docService.findById(did)
                .map(doc -> APIResponse.ok(docNodeService.getDocContent(did, 30)))
                .orElse(APIResponse.badRequest("文档不存在！"));
    }

    /**
     * @Description: 创建文档
     * @param title 文档名
     * @param owner 所有者
     * @return 成功或失败消息
     */
    @PostMapping()
    public APIResponse<?> createDoc(String title, String owner) {
        Doc doc = new Doc();
        doc.setTitle(title);
        boolean user = userService.userExist(owner);
        boolean group = groupService.groupExist(owner);
        if (group && !user) {
            doc.setOwner(new DocDto.Owner(owner, DocDto.OwnerEnum.Group));
            groupService.addGroupDoc(owner, docService.save(doc).toDocDto().getId());
            return APIResponse.ok("创建文档成功");
        } else if (user && !group) {
            doc.setOwner(new DocDto.Owner(owner, DocDto.OwnerEnum.User));
            docService.save(doc).toDocDto();
            return APIResponse.ok("创建文档成功");
        } else {
            return APIResponse.badRequest("文档所有者不存在！");
        }

    }

    /**
     * @Description: 更新文档信息
     * @param id 文档Id
     * @param title 文档名
     * @param owner 所有者
     * @param permission 文档Url权限
     * @param isDelete 是否在回收站
     * @return 成功或失败消息
     */
    @PutMapping("/{id}")
    public APIResponse<?> updateDoc(@PathVariable String id,
                                    String title,
                                    String owner,
                                    Permission permission,
                                    boolean isDelete) {
        return docService.findById(id)
                .map(doc -> {
                    doc.setTitle(title);
                    doc.createUrl(permission);
                    doc.setDelete(isDelete);
                    boolean user = userService.userExist(owner);
                    boolean group = groupService.groupExist(owner);
                    if (group && !user) {
                        doc.setOwner(new DocDto.Owner(owner, DocDto.OwnerEnum.Group));
                        groupService.addGroupDoc(owner, docService.save(doc).toDocDto().getId());
                    } else if (user && !group) {
                        doc.setOwner(new DocDto.Owner(owner, DocDto.OwnerEnum.User));
                        docService.save(doc);
                    } else {
                        return APIResponse.badRequest("文档所有者不存在！");
                    }
                    return APIResponse.ok("更新成功！");
                }).orElse(APIResponse.badRequest("文档不存在！"));
    }

    /**
     * @Description: 彻底删除文档
     * @param id 文档Id
     * @return 成功或失败消息
     */
    @DeleteMapping("/{id}")
    public APIResponse<?> deleteDoc(@PathVariable String id) {
        if (docService.exist(id))
            docService.delete(id);
        else
            return APIResponse.badRequest("文档不存在！");
        return APIResponse.ok("删除成功！");
    }

    /**
     * @Description: 查找用户所拥有的文档
     * @param owner 用户Id
     * @param page 分页
     * @param size 数量
     * @return List<DocDto>
     */
    @GetMapping("/find")
    public APIResponse<List<DocDto>> findByOwner(@RequestParam(required = false, defaultValue = "", value = "owner") String owner,
                                                 @RequestParam(required = false, defaultValue = "0", value = "p") Integer page,
                                                 @RequestParam(required = false, defaultValue = "10", value = "s") Integer size) {
        return APIResponse.ok(docService.findDocsByOwnerIs(owner, PageRequest.of(page, size))
                .stream().map(Doc::toDocDto).collect(Collectors.toList()));
    }

    /**
     * @Description: 统计信息
     * @return Statistic
     */
    @GetMapping("/statistic")
    public APIResponse<Statistic> getStatistic() {
        return APIResponse.ok(new Statistic(
                collabService.getOnlineUser(),
                userService.getUserSize(),
                groupService.getGroupSize(),
                docService.count()));
    }
}
