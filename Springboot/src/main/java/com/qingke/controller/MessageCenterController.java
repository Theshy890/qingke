package com.qingke.controller;

import com.qingke.common.Result;
import com.qingke.entity.MessageCenterDTO;
import com.qingke.service.MessageCenterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 消息中心控制器
 */
@RestController
@RequestMapping("/api/messageCenter")
public class MessageCenterController {

    @Autowired
    private MessageCenterService messageCenterService;

    /**
     * 获取消息中心未读总数
     */
    @GetMapping("/unreadTotal")
    public Result<MessageCenterDTO> getUnreadTotal(@RequestParam Long userId) {
        MessageCenterDTO dto = messageCenterService.getUnreadTotal(userId);
        return Result.success(dto);
    }
}
