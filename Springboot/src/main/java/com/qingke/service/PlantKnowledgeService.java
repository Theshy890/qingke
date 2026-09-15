package com.qingke.service;

import com.github.pagehelper.PageInfo;
import com.qingke.entity.PlantKnowledge;

import java.util.List;

/**
 * 养护知识服务接口
 */
public interface PlantKnowledgeService {
    
    /**
     * 查询所有养护知识
     */
    List<PlantKnowledge> findAll();
    
    /**
     * 分页查询养护知识
     */
    PageInfo<PlantKnowledge> findByPage(Integer pageNum, Integer pageSize, String plantName, String categoryName, String orderBy);
    
    /**
     * 根据ID查询养护知识
     */
    PlantKnowledge findById(Long id);
    
    /**
     * 新增养护知识
     */
    void add(PlantKnowledge plantKnowledge);
    
    /**
     * 更新养护知识
     */
    void update(PlantKnowledge plantKnowledge);
    
    /**
     * 根据ID删除养护知识
     */
    void deleteById(Long id);
    
    /**
     * 批量删除养护知识
     */
    void deleteBatch(List<Long> ids);
    
    /**
     * 增加点击次数
     */
    void increaseClicknum(Long id);
    
    /**
     * 增加收藏数量
     */
    void increaseStoreupnum(Long id);
    
    /**
     * 减少收藏数量
     */
    void decreaseStoreupnum(Long id);
}
