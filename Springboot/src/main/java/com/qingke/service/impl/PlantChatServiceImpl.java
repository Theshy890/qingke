package com.qingke.service.impl;

import com.qingke.entity.PlantChat;
import com.qingke.mapper.PlantChatMapper;
import com.qingke.service.PlantChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * AI聊天服务实现类
 */
@Service
public class PlantChatServiceImpl implements PlantChatService {

    @Autowired
    private PlantChatMapper plantChatMapper;

    @Override
    public void saveChat(PlantChat chat) {
        chat.setCreateTime(new Date());
        plantChatMapper.insert(chat);
    }

    @Override
    public List<PlantChat> findByUserId(Long userId) {
        return plantChatMapper.findByUserId(userId);
    }

    @Override
    public void deleteByUserId(Long userId) {
        plantChatMapper.deleteByUserId(userId);
    }
}
