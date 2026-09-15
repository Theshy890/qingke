package com.qingke.service;

import com.github.pagehelper.PageInfo;
import com.qingke.entity.PlantMaintainRecord;

import java.util.List;

/**
 * 养护记录服务接口
 */
public interface PlantMaintainRecordService {
    
    /**
     * 查询所有养护记录（管理员用）
     */
    List<PlantMaintainRecord> findAll();
    
    /**
     * 添加养护记录
     */
    void add(PlantMaintainRecord record);
    
    /**
     * 根据账号分页查询养护记录
     */
    PageInfo<PlantMaintainRecord> findByPage(Integer pageNum, Integer pageSize, String zhanghao);
    
    /**
     * 根据账号查询所有养护记录
     */
    List<PlantMaintainRecord> findByZhanghao(String zhanghao);
    
    /**
     * 根据ID查询
     */
    PlantMaintainRecord findById(Long id);
    
    /**
     * 更新养护记录
     */
    void update(PlantMaintainRecord record);
    
    /**
     * 删除养护记录
     */
    void deleteById(Long id);
    
    /**
     * 批量删除
     */
    void deleteBatch(List<Long> ids);
    
    /**
     * 根据绿植名称和账号查询养护记录
     */
    List<PlantMaintainRecord> findByPlantAndUser(String plantName, String userAccount);
}