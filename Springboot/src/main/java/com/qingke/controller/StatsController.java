package com.qingke.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qingke.common.Result;
import com.qingke.entity.*;
import com.qingke.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 仪表盘统计接口
 */
@RestController
@RequestMapping("/api/stats")
public class StatsController {

    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private PlantCategoryMapper plantCategoryMapper;
    @Autowired
    private PlantKnowledgeMapper plantKnowledgeMapper;
    @Autowired
    private PlantMaintainRecordMapper plantMaintainRecordMapper;
    @Autowired
    private CommunityPostMapper communityPostMapper;
    @Autowired
    private PlantRecognizeMapper plantRecognizeMapper;
    @Autowired
    private UserCollectMapper userCollectMapper;
    @Autowired
    private OperationLogMapper operationLogMapper;
    @Autowired
    private CommunityCommentMapper communityCommentMapper;
    @Autowired
    private SystemErrorMapper systemErrorMapper;

    /**
     * 仪表盘聚合统计
     */
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> dashboard() {
        Map<String, Object> stats = new HashMap<>();

        // 今天 00:00:00
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        Date todayDate = java.sql.Timestamp.valueOf(todayStart);

        // 本月第一天 00:00:00
        LocalDateTime monthStart = LocalDateTime.of(LocalDate.now().withDayOfMonth(1), LocalTime.MIN);
        Date monthDate = java.sql.Timestamp.valueOf(monthStart);

        // ===== 用户统计 =====
        Long userCount = sysUserMapper.selectCount(null);
        stats.put("userCount", userCount);

        Long todayNewUsers = sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().ge(SysUser::getRegisterTime, todayDate)
        );
        stats.put("todayNewUsers", todayNewUsers);

        // ===== 绿植种类 =====
        Long categoryCount = plantCategoryMapper.selectCount(null);
        stats.put("categoryCount", categoryCount);

        // ===== 养护知识 =====
        Long knowledgeCount = plantKnowledgeMapper.selectCount(null);
        stats.put("knowledgeCount", knowledgeCount);

        // ===== 养护记录 =====
        Long recordCount = plantMaintainRecordMapper.selectCount(null);
        stats.put("recordCount", recordCount);

        Long monthRecords = plantMaintainRecordMapper.selectCount(
                new LambdaQueryWrapper<PlantMaintainRecord>().ge(PlantMaintainRecord::getCreateTime, monthDate)
        );
        stats.put("monthRecords", monthRecords);

        // ===== 社区帖子 =====
        Long postCount = communityPostMapper.selectCount(null);
        stats.put("postCount", postCount);

        Long pendingPosts = communityPostMapper.selectCount(
                new LambdaQueryWrapper<CommunityPost>().eq(CommunityPost::getAuditStatus, 0)
        );
        stats.put("pendingPosts", pendingPosts);

        // ===== 社区评论 =====
        Long commentCount = communityCommentMapper.selectCount(null);
        stats.put("commentCount", commentCount);

        // ===== AI 识别 =====
        Long aiCount = plantRecognizeMapper.selectCount(null);
        stats.put("aiCount", aiCount);

        Long todayAi = plantRecognizeMapper.selectCount(
                new LambdaQueryWrapper<PlantRecognize>().ge(PlantRecognize::getCreateTime, todayDate)
        );
        stats.put("todayAi", todayAi);

        // ===== 收藏 =====
        Long storeupCount = userCollectMapper.selectCount(null);
        stats.put("storeupCount", storeupCount);

        // ===== 操作日志 =====
        Long logCount = operationLogMapper.selectCount(null);
        stats.put("logCount", logCount);

        // ===== 今日异常数 =====
        Long todayErrors = systemErrorMapper.selectCount(
                new LambdaQueryWrapper<SystemError>().ge(SystemError::getCreateTime, todayDate)
        );
        stats.put("todayErrors", todayErrors);

        Long totalErrors = systemErrorMapper.selectCount(null);
        stats.put("totalErrors", totalErrors);

        // ===== 服务器启动时间（通过 Spring 上下文获取） =====
        // 前端自行计算运行时长，这里返回启动时间戳
        stats.put("serverStartTime", System.currentTimeMillis() - getUptimeMillis());

        // ===== CPU 使用率（系统级，兼容 Windows） =====
        try {
            OperatingSystemMXBean sunOsBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
            double cpuLoad = sunOsBean.getCpuLoad() * 100;  // 0.0~1.0 → 0~100
            if (cpuLoad < 0) cpuLoad = sunOsBean.getSystemLoadAverage() * 100 / sunOsBean.getAvailableProcessors();
            int cpuPercent = (int) Math.min(100, Math.max(0, Math.round(cpuLoad)));
            stats.put("cpuUsage", cpuPercent);
        } catch (Exception e) {
            stats.put("cpuUsage", 0);
        }

        // ===== 内存使用率（系统物理内存，非 JVM 堆） =====
        try {
            OperatingSystemMXBean sunOsBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
            long totalPhysMem = sunOsBean.getTotalMemorySize();
            long freePhysMem = sunOsBean.getFreeMemorySize();
            long usedPhysMem = totalPhysMem - freePhysMem;
            int memPercent = (int) Math.round((double) usedPhysMem / totalPhysMem * 100);
            stats.put("memoryUsage", memPercent);
            stats.put("memoryUsedMB", usedPhysMem / (1024 * 1024));
            stats.put("memoryTotalMB", totalPhysMem / (1024 * 1024));
        } catch (Exception e) {
            stats.put("memoryUsage", 0);
            stats.put("memoryUsedMB", 0);
            stats.put("memoryTotalMB", 0);
        }

        // ===== 磁盘使用率 =====
        File disk = new File("/");
        if (!disk.exists()) disk = new File("C:\\");
        long totalDisk = disk.getTotalSpace();
        long usableDisk = disk.getUsableSpace();
        long usedDisk = totalDisk - usableDisk;
        int diskPercent = totalDisk > 0 ? (int) Math.round((double) usedDisk / totalDisk * 100) : 0;
        stats.put("diskUsage", diskPercent);
        stats.put("diskUsedGB", usedDisk / (1024 * 1024 * 1024));
        stats.put("diskTotalGB", totalDisk / (1024 * 1024 * 1024));

        return Result.success(stats);
    }

    /**
     * 获取 JVM 运行时长（毫秒）
     */
    private long getUptimeMillis() {
        return java.lang.management.ManagementFactory.getRuntimeMXBean().getUptime();
    }

    /**
     * 查询最近错误日志（默认最近20条）
     */
    @GetMapping("/errors")
    public Result<List<SystemError>> recentErrors(
            @RequestParam(defaultValue = "20") int limit
    ) {
        LambdaQueryWrapper<SystemError> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(SystemError::getCreateTime)
               .last("LIMIT " + Math.min(limit, 100));
        List<SystemError> errors = systemErrorMapper.selectList(wrapper);
        return Result.success(errors);
    }

    /**
     * 今日错误数
     */
    @GetMapping("/errors/today")
    public Result<Map<String, Object>> todayErrorCount() {
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        Date todayDate = java.sql.Timestamp.valueOf(todayStart);
        Long count = systemErrorMapper.selectCount(
                new LambdaQueryWrapper<SystemError>().ge(SystemError::getCreateTime, todayDate)
        );
        Map<String, Object> result = new HashMap<>();
        result.put("todayErrors", count);
        return Result.success(result);
    }

    /**
     * 清空所有异常日志
     */
    @DeleteMapping("/errors/clear")
    public Result<String> clearErrors() {
        int count = systemErrorMapper.delete(null);
        return Result.success("已清空 " + count + " 条异常日志");
    }
}
