package com.qingke.controller;

import com.github.pagehelper.PageInfo;
import com.qingke.common.OperationLogAnnotation;
import com.qingke.common.Result;
import com.qingke.entity.CommunityPost;
import com.qingke.service.CommunityPostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/community-post")
public class CommunityPostController {

    @Autowired
    private CommunityPostService communityPostService;

    @GetMapping("/list")
    public Result<List<CommunityPost>> list() {
        try {
            return Result.success(communityPostService.findAll());
        } catch (Exception e) { return Result.error(e.getMessage()); }
    }

    @GetMapping("/page")
    public Result<PageInfo<CommunityPost>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String userAccount,
            @RequestParam(required = false) Integer auditStatus
    ) {
        try {
            return Result.success(communityPostService.findByPage(pageNum, pageSize, title, userAccount, auditStatus));
        } catch (Exception e) { return Result.error(e.getMessage()); }
    }

    @GetMapping("/{id}")
    public Result<CommunityPost> getById(@PathVariable Long id) {
        try { return Result.success(communityPostService.findById(id)); }
        catch (Exception e) { return Result.error(e.getMessage()); }
    }

    @GetMapping("/user/{userId}")
    public Result<List<CommunityPost>> getByUserId(@PathVariable Long userId) {
        try { return Result.success(communityPostService.findByUserId(userId)); }
        catch (Exception e) { return Result.error(e.getMessage()); }
    }

    @OperationLogAnnotation(module = "社区帖子", type = "add", description = "发布社区帖子")
    @PostMapping("/add")
    public Result<String> add(@RequestBody CommunityPost post) {
        try { communityPostService.add(post); return Result.success("添加成功"); }
        catch (Exception e) { return Result.error(e.getMessage()); }
    }

    @OperationLogAnnotation(module = "社区帖子", type = "update", description = "修改社区帖子")
    @PutMapping("/update")
    public Result<String> update(@RequestBody CommunityPost post) {
        try { communityPostService.update(post); return Result.success("更新成功"); }
        catch (Exception e) { return Result.error(e.getMessage()); }
    }

    @OperationLogAnnotation(module = "社区帖子", type = "delete", description = "删除社区帖子")
    @DeleteMapping("/{id}")
    public Result<String> deleteById(@PathVariable Long id) {
        try { communityPostService.deleteById(id); return Result.success("删除成功"); }
        catch (Exception e) { return Result.error(e.getMessage()); }
    }

    @OperationLogAnnotation(module = "社区帖子", type = "delete", description = "批量删除社区帖子")
    @DeleteMapping("/batch")
    public Result<String> deleteBatch(@RequestBody List<Long> ids) {
        try { communityPostService.deleteBatch(ids); return Result.success("批量删除成功"); }
        catch (Exception e) { return Result.error(e.getMessage()); }
    }

    @OperationLogAnnotation(module = "社区帖子", type = "update", description = "点赞帖子")
    @PutMapping("/{id}/thumbsup")
    public Result<String> thumbsup(@PathVariable Long id) {
        try { communityPostService.increaseLikeCount(id); return Result.success("点赞成功"); }
        catch (Exception e) { return Result.error(e.getMessage()); }
    }

    @PutMapping("/{id}/thumbsdown")
    public Result<String> thumbsdown(@PathVariable Long id) {
        try { communityPostService.decreaseLikeCount(id); return Result.success("取消点赞成功"); }
        catch (Exception e) { return Result.error(e.getMessage()); }
    }

    @PutMapping("/{id}/crazily")
    public Result<String> crazily(@PathVariable Long id) {
        try { communityPostService.increaseDislikeCount(id); return Result.success("操作成功"); }
        catch (Exception e) { return Result.error(e.getMessage()); }
    }

    @OperationLogAnnotation(module = "社区帖子", type = "audit", description = "审核社区帖子")
    @PutMapping("/{id}/audit")
    public Result<String> audit(
            @PathVariable Long id,
            @RequestParam Integer auditStatus,
            @RequestParam(required = false) String auditReply
    ) {
        try { communityPostService.updateAuditStatus(id, auditStatus, auditReply); return Result.success("审核成功"); }
        catch (Exception e) { return Result.error(e.getMessage()); }
    }

    @PutMapping("/{id}/storeup")
    public Result<String> increaseCollectCount(@PathVariable Long id) {
        try { communityPostService.increaseCollectCount(id); return Result.success("收藏成功"); }
        catch (Exception e) { return Result.error(e.getMessage()); }
    }

    @PutMapping("/{id}/unstoreup")
    public Result<String> decreaseCollectCount(@PathVariable Long id) {
        try { communityPostService.decreaseCollectCount(id); return Result.success("取消收藏成功"); }
        catch (Exception e) { return Result.error(e.getMessage()); }
    }
}
