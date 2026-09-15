package com.qingke.service;

import com.qingke.entity.PlantChat;

import java.util.List;

/**
 * AI聊天服务接口
 */
public interface PlantChatService {

    /**
     * 保存聊天记录
     */
    void saveChat(PlantChat chat);

    /**
     * 根据用户ID查询聊天记录
     */
    List<PlantChat> findByUserId(Long userId);

    /**
     * 根据用户ID删除聊天记录
     */
    void deleteByUserId(Long userId);
}
