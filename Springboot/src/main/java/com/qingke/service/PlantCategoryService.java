package com.qingke.service;

import com.qingke.entity.PlantCategory;
import com.github.pagehelper.PageInfo;

import java.util.List;

/**
 * 绿植种类服务接口
 */
public interface PlantCategoryService {
    
    /**
     * 查询所有绿植种类
     */
    List<PlantCategory> findAll();
    
    /**
     * 根据ID查询绿植种类
     */
    PlantCategory findById(Long id);
    
    /**
     * 分页查询绿植种类
     */
    PageInfo<PlantCategory> findByPage(Integer pageNum, Integer pageSize, String categoryName);
    
    /**
     * 新增绿植种类
     */
    void add(PlantCategory plantCategory);
    
    /**
     * 更新绿植种类
     */
    void update(PlantCategory plantCategory);
    
    /**
     * 根据ID删除绿植种类
     */
    void deleteById(Long id);
    
    /**
     * 批量删除绿植种类
     */
    void deleteBatch(List<Long> ids);
}
