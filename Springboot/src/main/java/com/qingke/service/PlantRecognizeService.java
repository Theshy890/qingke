package com.qingke.service;

import com.github.pagehelper.PageInfo;
import com.qingke.entity.AiRecognitionRequest;
import com.qingke.entity.AiRecognitionResponse;
import com.qingke.entity.PlantRecognize;

import java.util.List;

/**
 * 绿植识别服务接口
 */
public interface PlantRecognizeService {
    
    /**
     * AI识别绿植病害
     */
    AiRecognitionResponse recognizePlant(AiRecognitionRequest request);
    
    /**
     * 保存识别记录
     */
    void saveRecord(PlantRecognize record);
    
    /**
     * 根据用户ID查询识别历史
     */
    List<PlantRecognize> findByUserid(Long userid);
    
    /**
     * 分页查询识别历史
     */
    PageInfo<PlantRecognize> findByPage(Integer pageNum, Integer pageSize, Long userid);
    
    /**
     * 根据ID查询识别记录
     */
    PlantRecognize findById(Long id);
    
    /**
     * 删除识别记录
     */
    void deleteById(Long id);
    
    /**
     * 批量删除
     */
    void deleteBatch(List<Long> ids);
    
    /**
     * 查询所有识别记录
     */
    List<PlantRecognize> findAll();
}