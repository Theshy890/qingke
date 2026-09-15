package com.qingke.controller;

import com.qingke.common.JwtUtil;
import com.qingke.common.Result;
import com.qingke.entity.OperationLog;
import com.qingke.service.OperationLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/operation-log")
public class OperationLogController {

    private static final Logger logger = LoggerFactory.getLogger(OperationLogController.class);

    @Autowired
    private OperationLogService operationLogService;
    
    /**
     * 获取最近的操作日志
     */
    @GetMapping("/recent")
    public Result<List<OperationLog>> getRecentLogs(@RequestParam(defaultValue = "10") int limit) {
        try {
            List<OperationLog> logs = operationLogService.getRecentLogs(limit);
            return Result.success(logs);
        } catch (Exception e) {
            logger.error("获取日志失败", e);
            return Result.error("获取日志失败：" + e.getMessage());
        }
    }
    
    /**
     * 根据用户名获取日志
     */
    @GetMapping("/user/{username}")
    public Result<List<OperationLog>> getLogsByUsername(@PathVariable String username) {
        try {
            List<OperationLog> logs = operationLogService.getLogsByUsername(username);
            return Result.success(logs);
        } catch (Exception e) {
            logger.error("获取日志失败", e);
            return Result.error("获取日志失败：" + e.getMessage());
        }
    }
    
    /**
     * 添加操作日志
     */
    @PostMapping("/add")
    public Result<String> addLog(@RequestBody OperationLog log, HttpServletRequest request) {
        try {
            // 从请求头的token中获取用户名
            String username = "匿名用户";
            String role = "user";
            
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                try {
                    // 解析token获取用户信息
                    String zh = JwtUtil.getZhFromToken(token);
                    String userRole = JwtUtil.getRoleFromToken(token);
                    if (zh != null) {
                        username = zh;
                    }
                    if (userRole != null) {
                        role = userRole;
                    }
                } catch (Exception e) {
                    // Token解析失败，使用默认值
                }
            }
            
            // 设置用户名、角色和操作时间
            log.setUsername(username);
            log.setRole(role);
            log.setOperationTime(new Date());
            log.setResult("success"); // 默认成功
            
            operationLogService.addLog(log);
            return Result.success("日志记录成功");
        } catch (Exception e) {
            logger.error("日志记录失败", e);
            return Result.error("日志记录失败：" + e.getMessage());
        }
    }

    /**
     * 清空所有操作日志
     */
    @DeleteMapping("/clear")
    public Result<String> clearAllLogs() {
        try {
            int count = operationLogService.clearAll();
            return Result.success("已清空 " + count + " 条日志");
        } catch (Exception e) {
            logger.error("清空日志失败", e);
            return Result.error("清空日志失败：" + e.getMessage());
        }
    }
}