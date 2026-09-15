package com.qingke.config;

import com.qingke.common.job.GenerateCareRemindersJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Quartz 配置 —— 业务主力
 * 负责：生成养护提醒等核心任务
 */
@Configuration
public class QuartzConfig {

    // ========== Job: 每小时生成养护提醒 ==========

    @Bean
    public JobDetail generateCareRemindersJobDetail() {
        return JobBuilder.newJob(GenerateCareRemindersJob.class)
                .withIdentity("generateCareRemindersJob", "business")
                .withDescription("每小时从养护记录批量生成未来提醒")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger generateCareRemindersTrigger() {
        return TriggerBuilder.newTrigger()
                .forJob(generateCareRemindersJobDetail())
                .withIdentity("generateCareRemindersTrigger", "business")
                .withDescription("整点触发")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 * * * ?")
                        .withMisfireHandlingInstructionFireAndProceed())
                .build();
    }
}