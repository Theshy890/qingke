package com.qingke.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.qingke.entity.ConversationDTO;
import com.qingke.entity.SysUser;
import com.qingke.entity.UserMessage;
import com.qingke.mapper.SysUserMapper;
import com.qingke.mapper.UserMessageMapper;
import com.qingke.service.UserMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 私信服务实现类
 */
@Service
public class UserMessageServiceImpl implements UserMessageService {

    // 撤回时限：发送后 2 分钟内
    private static final long REVOKE_TIME_LIMIT = 2 * 60 * 1000L;

    @Autowired
    private UserMessageMapper userMessageMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Override
    public UserMessage sendMessage(Long senderId, Long receiverId, String content, String quoteContent) {
        UserMessage message = new UserMessage();
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(content);
        message.setQuoteContent(quoteContent);
        message.setIsRecalled(0);
        message.setSenderDeleted(0);
        message.setReceiverDeleted(0);
        message.setIsRead(0);
        userMessageMapper.insert(message);
        return message;
    }

    @Override
    public void revokeMessage(Long messageId, Long userId) {
        UserMessage msg = userMessageMapper.selectById(messageId);
        if (msg == null) {
            throw new RuntimeException("消息不存在");
        }
        if (!msg.getSenderId().equals(userId)) {
            throw new RuntimeException("只能撤回自己发送的消息");
        }
        if (msg.getIsRecalled() != null && msg.getIsRecalled() == 1) {
            throw new RuntimeException("消息已撤回");
        }
        long elapsed = System.currentTimeMillis() - msg.getCreateTime().getTime();
        if (elapsed > REVOKE_TIME_LIMIT) {
            throw new RuntimeException("超过 2 分钟，无法撤回");
        }

        UserMessage update = new UserMessage();
        update.setId(messageId);
        update.setIsRecalled(1);
        userMessageMapper.updateById(update);
    }

    @Override
    public void deleteMessage(Long messageId, Long userId) {
        UserMessage msg = userMessageMapper.selectById(messageId);
        if (msg == null) {
            throw new RuntimeException("消息不存在");
        }
        if (!msg.getSenderId().equals(userId) && !msg.getReceiverId().equals(userId)) {
            throw new RuntimeException("无权删除该消息");
        }

        UserMessage update = new UserMessage();
        update.setId(messageId);
        if (msg.getSenderId().equals(userId)) {
            update.setSenderDeleted(1);
        } else {
            update.setReceiverDeleted(1);
        }
        userMessageMapper.updateById(update);
    }

    @Override
    public List<ConversationDTO> getConversations(Long userId) {
        // 查询所有相关消息（作为发送者或接收者），排除本人已单边删除的
        QueryWrapper<UserMessage> wrapper = new QueryWrapper<>();
        wrapper.and(w -> w.eq("sender_id", userId).or().eq("receiver_id", userId));
        wrapper.not(w -> w.eq("sender_id", userId).eq("sender_deleted", 1));
        wrapper.not(w -> w.eq("receiver_id", userId).eq("receiver_deleted", 1));
        wrapper.orderByDesc("create_time");

        List<UserMessage> messages = userMessageMapper.selectList(wrapper);

        // 按对话用户分组
        Map<Long, ConversationDTO> conversationMap = new HashMap<>();

        for (UserMessage msg : messages) {
            Long targetUserId = msg.getSenderId().equals(userId) ? msg.getReceiverId() : msg.getSenderId();

            if (!conversationMap.containsKey(targetUserId)) {
                ConversationDTO conversation = new ConversationDTO();
                conversation.setUserId(targetUserId);
                conversation.setLastMessage(
                        msg.getIsRecalled() != null && msg.getIsRecalled() == 1 ? "[撤回了一条消息]" : msg.getContent());
                conversation.setLastMessageTime(msg.getCreateTime());

                // 查询用户信息
                SysUser targetUser = sysUserMapper.selectById(targetUserId);
                if (targetUser != null) {
                    conversation.setNickname(targetUser.getName());
                    conversation.setAvatarUrl(targetUser.getAvatarUrl());
                }

                // 计算未读数
                QueryWrapper<UserMessage> unreadWrapper = new QueryWrapper<>();
                unreadWrapper.eq("sender_id", targetUserId);
                unreadWrapper.eq("receiver_id", userId);
                unreadWrapper.eq("is_read", 0);
                unreadWrapper.eq("is_recalled", 0);
                unreadWrapper.eq("receiver_deleted", 0);
                conversation.setUnreadCount(userMessageMapper.selectCount(unreadWrapper).intValue());

                conversationMap.put(targetUserId, conversation);
            }
        }

        return new ArrayList<>(conversationMap.values());
    }

    @Override
    public PageInfo<UserMessage> getChatHistory(Long userId, Long targetUserId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        QueryWrapper<UserMessage> wrapper = new QueryWrapper<>();
        wrapper.and(w -> w.and(w1 -> w1.eq("sender_id", userId).eq("receiver_id", targetUserId))
                .or(w2 -> w2.eq("sender_id", targetUserId).eq("receiver_id", userId)));
        // 排除本人已单边删除的消息
        wrapper.not(w -> w.eq("sender_id", userId).eq("sender_deleted", 1));
        wrapper.not(w -> w.eq("receiver_id", userId).eq("receiver_deleted", 1));
        wrapper.orderByDesc("create_time");

        return new PageInfo<>(userMessageMapper.selectList(wrapper));
    }

    @Override
    public void markAsRead(Long userId, Long targetUserId) {
        QueryWrapper<UserMessage> wrapper = new QueryWrapper<>();
        wrapper.eq("sender_id", targetUserId);
        wrapper.eq("receiver_id", userId);
        wrapper.eq("is_read", 0);

        UserMessage update = new UserMessage();
        update.setIsRead(1);
        userMessageMapper.update(update, wrapper);
    }

    @Override
    public Long getUnreadCount(Long userId) {
        return userMessageMapper.countUnread(userId);
    }
}
