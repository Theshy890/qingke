package com.qingke.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.qingke.entity.SysNotice;
import com.qingke.entity.PlantMaintainRecord;
import com.qingke.mapper.SysNoticeMapper;
import com.qingke.mapper.PlantMaintainRecordMapper;
import com.qingke.service.PlantMaintainRecordService;
import com.qingke.service.SysNoticeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class PlantMaintainRecordServiceImpl implements PlantMaintainRecordService {
    
    @Autowired
    private PlantMaintainRecordMapper plantMaintainRecordMapper;
    
    @Autowired
    private SysNoticeService sysNoticeService;
    
    @Autowired
    private SysNoticeMapper sysNoticeMapper;
    
    @Override
    public List<PlantMaintainRecord> findAll() {
        return plantMaintainRecordMapper.findAll();
    }
    
    @Override
    public void add(PlantMaintainRecord record) {
        record.setCreateTime(new Date());
        plantMaintainRecordMapper.insert(record);
        log.info("添加养护记录成功：{}", record.getPlantName());
        createReminderIfNeeded(record);
    }
    
    @Override
    public PageInfo<PlantMaintainRecord> findByPage(Integer pageNum, Integer pageSize, String zhanghao) {
        PageHelper.startPage(pageNum, pageSize);
        List<PlantMaintainRecord> list = plantMaintainRecordMapper.findByZhanghao(zhanghao);
        return new PageInfo<>(list);
    }
    
    @Override
    public List<PlantMaintainRecord> findByZhanghao(String zhanghao) {
        return plantMaintainRecordMapper.findByZhanghao(zhanghao);
    }
    
    @Override
    public PlantMaintainRecord findById(Long id) {
        return plantMaintainRecordMapper.findById(id);
    }
    
    @Override
    public void update(PlantMaintainRecord record) {
        plantMaintainRecordMapper.updateById(record);
        log.info("更新养护记录成功：ID={}", record.getId());
        createReminderIfNeeded(record);
    }
    
    @Override
    public void deleteById(Long id) {
        plantMaintainRecordMapper.deleteById(id);
        log.info("删除养护记录成功：ID={}", id);
    }
    
    @Override
    public void deleteBatch(List<Long> ids) {
        plantMaintainRecordMapper.deleteBatchIds(ids);
        log.info("批量删除养护记录成功：count={}", ids.size());
    }
    
    @Override
    public List<PlantMaintainRecord> findByPlantAndUser(String plantName, String userAccount) {
        return plantMaintainRecordMapper.findByPlantAndUser(plantName, userAccount);
    }
    
    private void createReminderIfNeeded(PlantMaintainRecord record) {
        if (record.getNextMaintainTime() == null || record.getUserId() == null) {
            return;
        }
        Date nextCareTime = record.getNextMaintainTime();
        Date now = new Date();
        long diffMillis = nextCareTime.getTime() - now.getTime();
        if (diffMillis > 7 * 24 * 60 * 60 * 1000) {
            return;
        }
        if (diffMillis < -24 * 60 * 60 * 1000) {
            return;
        }
        Long existingCount = sysNoticeMapper.selectCount(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysNotice>()
                .eq(SysNotice::getUserId, record.getUserId())
                .eq(SysNotice::getType, "个人提醒")
                .like(SysNotice::getContent, record.getPlantName())
                .eq(SysNotice::getIsRead, 0)
        );
        if (existingCount != null && existingCount > 0) {
            return;
        }
        Date actualRemindTime = diffMillis >= 0 ? nextCareTime : now;
        SysNotice reminder = new SysNotice();
        reminder.setUserId(record.getUserId());
        reminder.setType("个人提醒");
        reminder.setTitle("植物养护提醒");
        reminder.setBrief("您的" + record.getPlantName() + "需要养护了");
        reminder.setContent("您的植物【" + record.getPlantName() + "】需要进行养护了，请及时处理。");
        reminder.setRemindTime(actualRemindTime);
        reminder.setCreateTime(now);
        reminder.setIsRead(0);
        sysNoticeService.add(reminder);
        log.info("创建养护提醒: 用户ID={}, 植物={}", record.getUserId(), record.getPlantName());
    }
}
