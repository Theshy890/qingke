package com.qingke.service;

import com.qingke.entity.MessageCenterDTO;

/**
 * 消息中心服务接口
 */
public interface MessageCenterService {

    /**
     * 获取消息中心未读总数
     */
    MessageCenterDTO getUnreadTotal(Long userId);
}
