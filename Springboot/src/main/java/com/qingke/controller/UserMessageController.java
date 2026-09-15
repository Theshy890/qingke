package com.qingke.controller;

import com.github.pagehelper.PageInfo;
import com.qingke.common.Result;
import com.qingke.entity.ConversationDTO;
import com.qingke.entity.UserMessage;
import com.qingke.service.UserMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 私信控制器
 */
@RestController
@RequestMapping("/api/userMessage")
public class UserMessageController {

    @Autowired
    private UserMessageService userMessageService;

    /**
     * 发送私信
     */
    @PostMapping("/send")
    public Result<?> sendMessage(@RequestParam Long senderId, @RequestParam Long receiverId, @RequestParam String content) {
        userMessageService.sendMessage(senderId, receiverId, content);
        return Result.success("发送成功");
    }

    /**
     * 获取会话列表
     */
    @GetMapping("/conversations")
    public Result<List<ConversationDTO>> getConversations(@RequestParam Long userId) {
        List<ConversationDTO> conversations = userMessageService.getConversations(userId);
        return Result.success(conversations);
    }

    /**
     * 获取与某用户的聊天记录
     */
    @GetMapping("/detail")
    public Result<PageInfo<UserMessage>> getChatHistory(
            @RequestParam Long userId,
            @RequestParam Long targetUserId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        PageInfo<UserMessage> pageInfo = userMessageService.getChatHistory(userId, targetUserId, pageNum, pageSize);
        return Result.success(pageInfo);
    }

    /**
     * 标记与某用户的消息已读
     */
    @PutMapping("/markRead")
    public Result<?> markAsRead(@RequestParam Long userId, @RequestParam Long targetUserId) {
        userMessageService.markAsRead(userId, targetUserId);
        return Result.success("标记成功");
    }

    /**
     * 获取未读私信数量
     */
    @GetMapping("/unreadCount")
    public Result<Long> getUnreadCount(@RequestParam Long userId) {
        Long count = userMessageService.getUnreadCount(userId);
        return Result.success(count);
    }
}
