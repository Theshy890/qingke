package com.qingke.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qingke.entity.SysNotice;
import com.qingke.entity.PlantMaintainRecord;
import com.qingke.mapper.SysNoticeMapper;
import com.qingke.mapper.PlantMaintainRecordMapper;
import com.qingke.service.SysNoticeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
public class SysNoticeServiceImpl implements SysNoticeService {
    
    @Autowired
    private SysNoticeMapper sysNoticeMapper;
    
    @Autowired
    private PlantMaintainRecordMapper plantMaintainRecordMapper;
    
    @Override
    public Page<SysNotice> page(int pageNum, int pageSize, Long userId, List<String> type, Integer isRead) {
        Page<SysNotice> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysNotice> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) wrapper.eq(SysNotice::getUserId, userId);
        if (type != null && !type.isEmpty()) wrapper.in(SysNotice::getType, type);
        if (isRead != null) wrapper.eq(SysNotice::getIsRead, isRead);
        wrapper.orderByDesc(SysNotice::getRemindTime);
        return sysNoticeMapper.selectPage(page, wrapper);
    }
    
    @Override
    public List<SysNotice> findByUserId(Long userId) {
        LambdaQueryWrapper<SysNotice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysNotice::getUserId, userId);
        wrapper.orderByDesc(SysNotice::getRemindTime);
        return sysNoticeMapper.selectList(wrapper);
    }
    
    @Override
    public SysNotice findById(Long id) { return sysNoticeMapper.selectById(id); }
    
    @Override
    @Transactional
    public void add(SysNotice notice) {
        if (notice.getCreateTime() == null) notice.setCreateTime(new Date());
        if (notice.getRemindTime() == null) notice.setRemindTime(notice.getCreateTime());
        if (notice.getIsRead() == null) notice.setIsRead(0);
        sysNoticeMapper.insert(notice);
    }
    
    @Override
    @Transactional
    public void update(SysNotice notice) { sysNoticeMapper.updateById(notice); }
    
    @Override
    @Transactional
    public void deleteById(Long id) { sysNoticeMapper.deleteById(id); }
    
    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) { sysNoticeMapper.deleteBatchIds(ids); }

    @Override
    @Transactional
    public void deleteBatchByContent(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return;

        // 获取选中的通知记录
        List<SysNotice> selectedNotices = sysNoticeMapper.selectBatchIds(ids);
        if (selectedNotices == null || selectedNotices.isEmpty()) return;

        // 对于每个选中的通知，根据标题和内容删除所有用户的相同通知
        for (SysNotice notice : selectedNotices) {
            LambdaQueryWrapper<SysNotice> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysNotice::getTitle, notice.getTitle())
                   .eq(SysNotice::getContent, notice.getContent())
                   .eq(SysNotice::getType, notice.getType());
            sysNoticeMapper.delete(wrapper);
        }

        log.info("批量删除通知完成，共删除 {} 个通知模板及其所有用户副本", selectedNotices.size());
    }
    
    @Override
    @Transactional
    public void markAsRead(Long id) {
        SysNotice notice = sysNoticeMapper.selectById(id);
        if (notice != null) {
            notice.setIsRead(1);
            sysNoticeMapper.updateById(notice);
        }
    }
    
    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        LambdaQueryWrapper<SysNotice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysNotice::getUserId, userId)
               .eq(SysNotice::getIsRead, 0);
        List<SysNotice> unreadList = sysNoticeMapper.selectList(wrapper);
        for (SysNotice notice : unreadList) {
            notice.setIsRead(1);
            sysNoticeMapper.updateById(notice);
        }
    }
    
    @Override
    @Transactional
    public void generateReminders() {
        log.info("开始生成养护提醒...");
        try {
            LambdaQueryWrapper<PlantMaintainRecord> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByDesc(PlantMaintainRecord::getCreateTime);
            List<PlantMaintainRecord> recordList = plantMaintainRecordMapper.selectList(wrapper);
            if (recordList == null || recordList.isEmpty()) { log.info("没有养护记录"); return; }
            
            Date now = new Date();
            int createdCount = 0;
            
            for (PlantMaintainRecord jilu : recordList) {
                LambdaQueryWrapper<SysNotice> remindWrapper = new LambdaQueryWrapper<>();
                remindWrapper.eq(SysNotice::getUserId, jilu.getUserId());
                remindWrapper.eq(SysNotice::getType, "个人提醒");
                remindWrapper.like(SysNotice::getContent, jilu.getPlantName());
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.HOUR_OF_DAY, -24);
                remindWrapper.ge(SysNotice::getCreateTime, cal.getTime());
                if (sysNoticeMapper.selectCount(remindWrapper) > 0) continue;
                
                Date nextRemindTime = calculateNextRemindTime(jilu);
                if (nextRemindTime == null) continue;
                
                long diffMillis = nextRemindTime.getTime() - now.getTime();
                if (diffMillis > 2 * 60 * 60 * 1000 || diffMillis < -24 * 60 * 60 * 1000) continue;
                
                Date actualRemindTime = diffMillis >= 0 ? nextRemindTime : now;
                SysNotice reminder = new SysNotice();
                reminder.setUserId(jilu.getUserId());
                reminder.setType("个人提醒");
                reminder.setTitle("植物养护提醒");
                reminder.setBrief("您的植物需要养护了");
                reminder.setContent("您的植物【" + jilu.getPlantName() + "】需要进行养护了，请及时处理。");
                reminder.setRemindTime(actualRemindTime);
                reminder.setCreateTime(now);
                reminder.setIsRead(0);
                
                if (sysNoticeMapper.insert(reminder) > 0) createdCount++;
            }
            log.info("养护提醒生成完成，共创建 {} 条", createdCount);
        } catch (Exception e) {
            log.error("生成养护提醒失败", e);
            throw new RuntimeException("生成养护提醒失败: " + e.getMessage());
        }
    }
    
    private Date calculateNextRemindTime(PlantMaintainRecord jilu) {
        if (jilu.getNextMaintainTime() != null) return jilu.getNextMaintainTime();
        Date baseTime = null;
        if (jilu.getMaintainDate() != null && !jilu.getMaintainDate().isEmpty()) {
            try { baseTime = new java.text.SimpleDateFormat("yyyy-MM-dd").parse(jilu.getMaintainDate()); }
            catch (Exception e) { log.warn("养护日期解析失败"); }
        }
        if (baseTime == null) baseTime = jilu.getCreateTime();
        if (baseTime == null) return null;
        
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(baseTime);
        String cycle = jilu.getMaintainCycle();
        if (cycle != null && !cycle.isEmpty()) {
            if (cycle.contains("天")) { try { calendar.add(Calendar.DAY_OF_MONTH, Integer.parseInt(cycle.replaceAll("\\D+", ""))); } catch (Exception e) { calendar.add(Calendar.DAY_OF_MONTH, 7); } }
            else if (cycle.contains("周")) { try { calendar.add(Calendar.WEEK_OF_YEAR, Integer.parseInt(cycle.replaceAll("\\D+", ""))); } catch (Exception e) { calendar.add(Calendar.DAY_OF_MONTH, 7); } }
            else if (cycle.contains("月")) { try { calendar.add(Calendar.MONTH, Integer.parseInt(cycle.replaceAll("\\D+", ""))); } catch (Exception e) { calendar.add(Calendar.DAY_OF_MONTH, 30); } }
            else calendar.add(Calendar.DAY_OF_MONTH, 7);
        } else calendar.add(Calendar.DAY_OF_MONTH, 7);
        return calendar.getTime();
    }
}
