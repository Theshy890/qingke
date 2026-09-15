package com.qingke.controller;

import com.qingke.common.OperationLogAnnotation;
import com.qingke.common.Result;
import com.qingke.entity.CommentReplyDTO;
import com.qingke.entity.CommentTreeDTO;
import com.qingke.entity.CommunityComment;
import com.qingke.service.CommunityCommentService;
import com.qingke.service.CommunityPostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/community-comment")
public class CommunityCommentController {

    @Autowired
    private CommunityCommentService communityCommentService;

    @Autowired
    private CommunityPostService communityPostService;

    @GetMapping("/list/{postId}")
    public Result<List<CommunityComment>> list(@PathVariable Long postId) {
        try {
            List<CommunityComment> list = communityCommentService.findByPostId(postId);
            return Result.success(list);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // 获取评论树（主评论+回复列表）
    @GetMapping("/tree/{postId}")
    public Result<List<CommentTreeDTO>> getCommentTree(@PathVariable Long postId) {
        try {
            List<CommentTreeDTO> tree = communityCommentService.getCommentTree(postId);
            return Result.success(tree);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // 获取某个主评论下的回复列表（分页）
    @GetMapping("/replies/{parentId}")
    public Result<List<CommentReplyDTO>> getReplies(
            @PathVariable Long parentId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "3") Integer pageSize) {
        try {
            List<CommentReplyDTO> replies = communityCommentService.getRepliesByParentId(parentId, pageNum, pageSize);
            return Result.success(replies);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @OperationLogAnnotation(module = "社区评论", type = "add", description = "发表评论")
    @PostMapping("/add")
    public Result<String> add(@RequestBody CommunityComment comment) {
        try {
            communityCommentService.add(comment);
            return Result.success("评论成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // 回复评论
    @OperationLogAnnotation(module = "社区评论", type = "add", description = "回复评论")
    @PostMapping("/reply")
    public Result<String> replyComment(@RequestBody CommunityComment comment) {
        try {
            communityCommentService.replyComment(comment);
            return Result.success("回复成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @OperationLogAnnotation(module = "社区评论", type = "delete", description = "删除评论")
    @DeleteMapping("/{id}")
    public Result<String> deleteById(@PathVariable Long id) {
        try {
            communityCommentService.deleteById(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/thumbsup")
    public Result<String> thumbsup(@PathVariable Long id) {
        try {
            communityCommentService.increaseLikeCount(id);
            return Result.success("点赞成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/crazily")
    public Result<String> crazily(@PathVariable Long id) {
        try {
            communityCommentService.increaseDislikeCount(id);
            return Result.success("操作成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @OperationLogAnnotation(module = "社区评论", type = "update", description = "回复评论")
    @PostMapping("/{id}/reply")
    public Result<String> reply(@PathVariable Long id, @RequestParam String reply) {
        try {
            communityCommentService.updateReply(id, reply);
            return Result.success("回复成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
