package com.qingke.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qingke.entity.SysNotice;
import java.util.List;

public interface SysNoticeService {
    Page<SysNotice> page(int pageNum, int pageSize, Long userId, List<String> type, Integer isRead);
    List<SysNotice> findByUserId(Long userId);
    SysNotice findById(Long id);
    void add(SysNotice notice);
    void update(SysNotice notice);
    void deleteById(Long id);
    void deleteBatch(List<Long> ids);
    void deleteBatchByContent(List<Long> ids); // 根据内容删除所有用户的相同通知
    void markAsRead(Long id);
    void markAllAsRead(Long userId);
    void generateReminders();
}
