package com.qingke.controller;

import com.github.pagehelper.PageInfo;
import com.qingke.common.Result;
import com.qingke.entity.VisitorDTO;
import com.qingke.service.UserVisitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 访客记录控制器
 */
@RestController
@RequestMapping("/api/userVisitor")
public class UserVisitorController {

    @Autowired
    private UserVisitorService userVisitorService;

    /**
     * 记录访问
     */
    @PostMapping("/record")
    public Result<?> recordVisit(@RequestParam Long userId, @RequestParam Long visitorId) {
        userVisitorService.recordVisit(userId, visitorId);
        return Result.success("记录成功");
    }

    /**
     * 获取访客列表
     */
    @GetMapping("/list")
    public Result<PageInfo<VisitorDTO>> getVisitorList(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        PageInfo<VisitorDTO> pageInfo = userVisitorService.getVisitorList(userId, pageNum, pageSize);
        return Result.success(pageInfo);
    }

    /**
     * 标记访客记录已读
     */
    @PutMapping("/markRead")
    public Result<?> markAsRead(@RequestParam Long userId) {
        userVisitorService.markAsRead(userId);
        return Result.success("标记成功");
    }

    /**
     * 获取未读访客数量
     */
    @GetMapping("/unreadCount")
    public Result<Long> getUnreadCount(@RequestParam Long userId) {
        Long count = userVisitorService.getUnreadCount(userId);
        return Result.success(count);
    }
}
