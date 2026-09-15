package com.qingke.common.job;

import com.qingke.service.SysNoticeService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Quartz Job —— 业务主力：每小时生成养护提醒
 * 从养护记录自动计算下次提醒时间并写入数据库
 */
@Component
public class GenerateCareRemindersJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(GenerateCareRemindersJob.class);

    @Autowired
    private SysNoticeService sysNoticeService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        logger.info("[quartz-worker] 开始生成养护提醒...");
        try {
            sysNoticeService.generateReminders();
            logger.info("[quartz-worker] 养护提醒生成完成");
        } catch (Exception e) {
            logger.error("[quartz-worker] 生成养护提醒失败", e);
            throw new JobExecutionException("生成养护提醒失败", e);
        }
    }
}
