package com.qingke.service;

import com.github.pagehelper.PageInfo;
import com.qingke.entity.VisitorDTO;

/**
 * 访客记录服务接口
 */
public interface UserVisitorService {

    /**
     * 记录访问
     */
    void recordVisit(Long userId, Long visitorId);

    /**
     * 获取访客列表
     */
    PageInfo<VisitorDTO> getVisitorList(Long userId, int pageNum, int pageSize);

    /**
     * 标记访客记录已读
     */
    void markAsRead(Long userId);

    /**
     * 获取未读访客数量
     */
    Long getUnreadCount(Long userId);
}
