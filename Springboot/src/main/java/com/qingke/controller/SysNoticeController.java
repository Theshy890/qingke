package com.qingke.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qingke.common.OperationLogAnnotation;
import com.qingke.common.Result;
import com.qingke.entity.SysNotice;
import com.qingke.mapper.SysNoticeMapper;
import com.qingke.service.SysNoticeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/sys-notice")
public class SysNoticeController {
    
    @Autowired
    private SysNoticeService sysNoticeService;
    
    @Autowired
    private SysNoticeMapper sysNoticeMapper;
    
    @GetMapping("/page")
    public Result<Page<SysNotice>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) List<String> type,
            @RequestParam(required = false) Integer isRead
    ) {
        Page<SysNotice> page = sysNoticeService.page(pageNum, pageSize, userId, type, isRead);
        return Result.success(page);
    }
    
    @GetMapping("/user/{userId}")
    public Result<List<SysNotice>> getByUserId(@PathVariable Long userId) {
        List<SysNotice> list = sysNoticeService.findByUserId(userId);
        return Result.success(list);
    }
    
    @GetMapping("/unread/{userId}")
    public Result<Long> getUnreadCount(@PathVariable Long userId) {
        LambdaQueryWrapper<SysNotice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysNotice::getUserId, userId)
               .eq(SysNotice::getIsRead, 0);
        long count = sysNoticeMapper.selectCount(wrapper);
        return Result.success(count);
    }
    
    @GetMapping("/{id}")
    public Result<SysNotice> getById(@PathVariable Long id) {
        SysNotice notice = sysNoticeService.findById(id);
        if (notice == null) return Result.error("通知不存在");
        return Result.success(notice);
    }
    
    @PostMapping("/add")
    @OperationLogAnnotation(module = "通知公告", type = "add", description = "新增通知公告")
    public Result<String> add(@RequestBody SysNotice notice) {
        sysNoticeService.add(notice);
        return Result.success("添加成功");
    }
    
    @PutMapping("/update")
    @OperationLogAnnotation(module = "通知公告", type = "update", description = "修改通知公告")
    public Result<String> update(@RequestBody SysNotice notice) {
        sysNoticeService.update(notice);
        return Result.success("更新成功");
    }
    
    @DeleteMapping("/{id}")
    @OperationLogAnnotation(module = "通知公告", type = "delete", description = "删除通知公告")
    public Result<String> delete(@PathVariable Long id) {
        sysNoticeService.deleteById(id);
        return Result.success("删除成功");
    }
    
    @DeleteMapping("/batch")
    @OperationLogAnnotation(module = "通知公告", type = "delete", description = "批量删除通知公告")
    public Result<String> deleteBatch(@RequestBody List<Long> ids) {
        sysNoticeService.deleteBatch(ids);
        return Result.success("删除成功");
    }

    @DeleteMapping("/batch-all-users")
    @OperationLogAnnotation(module = "通知公告", type = "delete", description = "批量删除通知公告（所有用户）")
    public Result<String> deleteBatchAllUsers(@RequestBody List<Long> ids) {
        sysNoticeService.deleteBatchByContent(ids);
        return Result.success("删除成功");
    }
    
    @PutMapping("/read/{id}")
    @OperationLogAnnotation(module = "通知公告", type = "update", description = "标记通知已读")
    public Result<String> markAsRead(@PathVariable Long id) {
        sysNoticeService.markAsRead(id);
        return Result.success("操作成功");
    }

    @PutMapping("/read/all/{userId}")
    public Result<String> markAllAsRead(@PathVariable Long userId) {
        sysNoticeService.markAllAsRead(userId);
        return Result.success("全部标记已读成功");
    }
    
    @PostMapping("/generate")
    @OperationLogAnnotation(module = "通知公告", type = "execute", description = "手动生成养护提醒")
    public Result<String> generateReminders() {
        sysNoticeService.generateReminders();
        return Result.success("提醒生成任务已执行");
    }
}
