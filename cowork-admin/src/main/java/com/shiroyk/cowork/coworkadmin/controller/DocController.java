package com.shiroyk.cowork.coworkadmin.controller;

import com.shiroyk.cowork.coworkadmin.dto.UpdateDoc;
import com.shiroyk.cowork.coworkadmin.service.CollabService;
import com.shiroyk.cowork.coworkadmin.service.DocService;
import com.shiroyk.cowork.coworkadmin.service.GroupService;
import com.shiroyk.cowork.coworkadmin.service.UserService;
import com.shiroyk.cowork.coworkcommon.dto.APIResponse;
import com.shiroyk.cowork.coworkcommon.dto.DocDto;
import com.shiroyk.cowork.coworkcommon.dto.Statistic;
import com.shiroyk.cowork.coworkcommon.model.doc.Doc;
import com.shiroyk.cowork.coworkcommon.model.doc.Owner;
import com.shiroyk.cowork.coworkcommon.model.group.Group;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/admin/doc")
public class DocController {
    private final DocService docService;
    private final GroupService groupService;
    private final UserService userService;
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
                        return userService.findById(doc.getOwnerId())
                                .map(user -> APIResponse.ok(doc.toDocDto(user.toUserDtoL())))
                                .orElse(APIResponse.ok(doc.toDocDto()));
                    } else {
                        return groupService.findById(doc.getOwnerId())
                                .map(group -> APIResponse.ok(doc.toDocDto(group.toGroupDto())))
                                .orElse(APIResponse.ok(doc.toDocDto()));
                    }
                })
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

        if (userService.findById(owner).isPresent()) {
            doc.setOwner(new Owner(owner, Owner.OwnerEnum.User));
            docService.save(doc);
        } else {
            Optional<Group> group = groupService.findById(owner);

            if (!group.isPresent()) return APIResponse.badRequest("文档所有者不存在！");

            doc.setOwner(new Owner(owner, Owner.OwnerEnum.Group));
            docService.save(doc);

            Group g = group.get();
            g.getDocs().add(doc.getId());
            groupService.save(g);
        } return APIResponse.ok("创建文档成功");
    }

    /**
     * @Description: 更新文档信息
     * @param id 文档Id
     * @param updateDoc 文档信息
     * @return 成功或失败消息
     */
    @PutMapping("/{id}")
    public APIResponse<?> updateDoc(@PathVariable String id,
                                    @Valid UpdateDoc updateDoc) {
        return docService.findById(id)
                .map(doc -> {
                    doc.setTitle(updateDoc.getTitle());
                    doc.createUrl(updateDoc.getPermission());
                    doc.setDelete(updateDoc.isDelete());

                    if (userService.findById(updateDoc.getOwner()).isPresent()) {
                        doc.setOwner(new Owner(updateDoc.getOwner(), Owner.OwnerEnum.User));
                        docService.save(doc);
                        return APIResponse.ok("更新成功！");
                    } else {
                        // 将文档从旧群组删除
                        groupService.findById(doc.getOwnerId())
                                .ifPresent(group -> {
                                    group.getDocs().remove(doc.getId());
                                    groupService.save(group);
                                });

                        doc.setOwner(new Owner(updateDoc.getOwner(), Owner.OwnerEnum.Group));

                        // 将文档添加到新群组
                        return groupService.findById(updateDoc.getOwner())
                                .map(group -> {
                                    docService.save(doc);
                                    group.getDocs().add(doc.getId());
                                    groupService.save(group);
                                    return APIResponse.ok("更新成功！");
                                }).orElse(APIResponse.badRequest("文档所有者不存在！"));
                    }
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
                userService.count(),
                groupService.count(),
                docService.count()));
    }
}
