package com.qingke.service;

import com.github.pagehelper.PageInfo;
import com.qingke.entity.ConversationDTO;
import com.qingke.entity.UserMessage;

import java.util.List;

/**
 * 私信服务接口
 */
public interface UserMessageService {

    /**
     * 发送私信
     */
    void sendMessage(Long senderId, Long receiverId, String content);

    /**
     * 获取会话列表
     */
    List<ConversationDTO> getConversations(Long userId);

    /**
     * 获取与某用户的聊天记录
     */
    PageInfo<UserMessage> getChatHistory(Long userId, Long targetUserId, int pageNum, int pageSize);

    /**
     * 标记与某用户的消息已读
     */
    void markAsRead(Long userId, Long targetUserId);

    /**
     * 获取未读私信数量
     */
    Long getUnreadCount(Long userId);
}
