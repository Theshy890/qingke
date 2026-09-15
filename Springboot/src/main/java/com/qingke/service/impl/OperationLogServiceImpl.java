package com.qingke.service.impl;

import com.qingke.entity.OperationLog;
import com.qingke.mapper.OperationLogMapper;
import com.qingke.service.OperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OperationLogServiceImpl implements OperationLogService {
    
    @Autowired
    private OperationLogMapper operationLogMapper;
    
    @Override
    public void addLog(OperationLog log) {
        operationLogMapper.insert(log);
    }
    
    @Override
    public List<OperationLog> getRecentLogs(int limit) {
        return operationLogMapper.findRecentLogs(limit);
    }
    
    @Override
    public List<OperationLog> getLogsByUsername(String username) {
        return operationLogMapper.findByUsername(username);
    }

    @Override
    public int clearAll() {
        return operationLogMapper.delete(null);
    }
}
