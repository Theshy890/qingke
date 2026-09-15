package com.qingke.service.impl;

import com.qingke.entity.MessageCenterDTO;
import com.qingke.service.MessageCenterService;
import com.qingke.service.UserMessageService;
import com.qingke.service.UserVisitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 消息中心服务实现类
 */
@Service
public class MessageCenterServiceImpl implements MessageCenterService {

    @Autowired
    private UserMessageService userMessageService;

    @Autowired
    private UserVisitorService userVisitorService;

    @Override
    public MessageCenterDTO getUnreadTotal(Long userId) {
        MessageCenterDTO dto = new MessageCenterDTO();

        Long unreadMessages = userMessageService.getUnreadCount(userId);
        Long unreadVisitors = userVisitorService.getUnreadCount(userId);

        dto.setUnreadMessages(unreadMessages);
        dto.setUnreadVisitors(unreadVisitors);
        dto.setUnreadFollowers(0L); // 暂时设为0，后续可以扩展
        dto.setTotal(unreadMessages + unreadVisitors);

        return dto;
    }
}
