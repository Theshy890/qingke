package com.qingke.service.impl;

import com.qingke.entity.PlantCategory;
import com.qingke.mapper.PlantCategoryMapper;
import com.qingke.service.PlantCategoryService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 绿植种类服务实现类
 */
@Service
@Transactional
public class PlantCategoryServiceImpl implements PlantCategoryService {
    
    @Autowired
    private PlantCategoryMapper plantCategoryMapper;
    
    @Override
    public List<PlantCategory> findAll() {
        return plantCategoryMapper.selectAll();
    }
    
    @Override
    public PlantCategory findById(Long id) {
        if (id == null) {
            throw new RuntimeException("ID不能为空");
        }
        PlantCategory plantCategory = plantCategoryMapper.selectById(id);
        if (plantCategory == null) {
            throw new RuntimeException("绿植种类不存在");
        }
        return plantCategory;
    }
    
    @Override
    public PageInfo<PlantCategory> findByPage(Integer pageNum, Integer pageSize, String categoryName) {
        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }
        PageHelper.startPage(pageNum, pageSize);
        List<PlantCategory> list = plantCategoryMapper.selectByPage(categoryName);
        return new PageInfo<>(list);
    }
    
    @Override
    public void add(PlantCategory plantCategory) {
        if (plantCategory == null) {
            throw new RuntimeException("绿植种类信息不能为空");
        }
        if (plantCategory.getCategoryName() == null || plantCategory.getCategoryName().trim().isEmpty()) {
            throw new RuntimeException("绿植种类名称不能为空");
        }
        
        int count = plantCategoryMapper.checkNameExists(plantCategory.getCategoryName(), null);
        if (count > 0) {
            throw new RuntimeException("该绿植种类已存在");
        }
        
        plantCategory.setCreateTime(new Date());
        int result = plantCategoryMapper.insert(plantCategory);
        if (result <= 0) {
            throw new RuntimeException("添加绿植种类失败");
        }
    }
    
    @Override
    public void update(PlantCategory plantCategory) {
        if (plantCategory == null || plantCategory.getId() == null) {
            throw new RuntimeException("绿植种类ID不能为空");
        }
        if (plantCategory.getCategoryName() == null || plantCategory.getCategoryName().trim().isEmpty()) {
            throw new RuntimeException("绿植种类名称不能为空");
        }
        
        PlantCategory existing = plantCategoryMapper.selectById(plantCategory.getId());
        if (existing == null) {
            throw new RuntimeException("绿植种类不存在");
        }
        
        int count = plantCategoryMapper.checkNameExists(plantCategory.getCategoryName(), plantCategory.getId());
        if (count > 0) {
            throw new RuntimeException("该绿植种类已存在");
        }
        
        int result = plantCategoryMapper.updateById(plantCategory);
        if (result <= 0) {
            throw new RuntimeException("更新绿植种类失败");
        }
    }
    
    @Override
    public void deleteById(Long id) {
        if (id == null) {
            throw new RuntimeException("ID不能为空");
        }
        
        PlantCategory plantCategory = plantCategoryMapper.selectById(id);
        if (plantCategory == null) {
            throw new RuntimeException("绿植种类不存在");
        }
        
        int result = plantCategoryMapper.deleteById(id);
        if (result <= 0) {
            throw new RuntimeException("删除绿植种类失败");
        }
    }
    
    @Override
    public void deleteBatch(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new RuntimeException("删除的ID列表不能为空");
        }
        
        int result = plantCategoryMapper.deleteBatch(ids);
        if (result <= 0) {
            throw new RuntimeException("批量删除绿植种类失败");
        }
    }
}
