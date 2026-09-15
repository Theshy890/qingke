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

    @Autowired
    private UserMessageMapper userMessageMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Override
    public void sendMessage(Long senderId, Long receiverId, String content) {
        UserMessage message = new UserMessage();
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(content);
        message.setIsRead(0);
        userMessageMapper.insert(message);
    }

    @Override
    public List<ConversationDTO> getConversations(Long userId) {
        // 查询所有相关消息（作为发送者或接收者）
        QueryWrapper<UserMessage> wrapper = new QueryWrapper<>();
        wrapper.and(w -> w.eq("sender_id", userId).or().eq("receiver_id", userId));
        wrapper.orderByDesc("create_time");

        List<UserMessage> messages = userMessageMapper.selectList(wrapper);

        // 按对话用户分组
        Map<Long, ConversationDTO> conversationMap = new HashMap<>();

        for (UserMessage msg : messages) {
            Long targetUserId = msg.getSenderId().equals(userId) ? msg.getReceiverId() : msg.getSenderId();

            if (!conversationMap.containsKey(targetUserId)) {
                ConversationDTO conversation = new ConversationDTO();
                conversation.setUserId(targetUserId);
                conversation.setLastMessage(msg.getContent());
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
