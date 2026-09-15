package com.qingke.service;

import com.qingke.entity.OperationLog;
import java.util.List;

public interface OperationLogService {
    
    /**
     * 添加操作日志
     */
    void addLog(OperationLog log);
    
    /**
     * 获取最近的日志
     */
    List<OperationLog> getRecentLogs(int limit);
    
    /**
     * 根据用户名获取日志
     */
    List<OperationLog> getLogsByUsername(String username);

    /**
     * 清空所有日志
     */
    int clearAll();
}