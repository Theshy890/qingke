package com.qingke.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.qingke.entity.PlantKnowledge;
import com.qingke.mapper.PlantKnowledgeMapper;
import com.qingke.service.PlantKnowledgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 养护知识服务实现类
 */
@Service
public class PlantKnowledgeServiceImpl implements PlantKnowledgeService {
    
    @Autowired
    private PlantKnowledgeMapper plantKnowledgeMapper;
    
    @Override
    public List<PlantKnowledge> findAll() {
        return plantKnowledgeMapper.findAll();
    }
    
    @Override
    public PageInfo<PlantKnowledge> findByPage(Integer pageNum, Integer pageSize, String plantName, String categoryName, String orderBy) {
        PageHelper.startPage(pageNum, pageSize);
        List<PlantKnowledge> list = plantKnowledgeMapper.findByCondition(plantName, categoryName, orderBy);
        return new PageInfo<>(list);
    }
    
    @Override
    public PlantKnowledge findById(Long id) {
        return plantKnowledgeMapper.findById(id);
    }
    
    @Override
    public void add(PlantKnowledge plantKnowledge) {
        plantKnowledge.setCreateTime(new Date());
        plantKnowledge.setPublishDate(new Date());
        plantKnowledge.setClickNum(0);
        plantKnowledge.setCollectNum(0);
        plantKnowledgeMapper.insert(plantKnowledge);
    }
    
    @Override
    public void update(PlantKnowledge plantKnowledge) {
        plantKnowledgeMapper.updateById(plantKnowledge);
    }
    
    @Override
    public void deleteById(Long id) {
        plantKnowledgeMapper.deleteById(id);
    }
    
    @Override
    public void deleteBatch(List<Long> ids) {
        plantKnowledgeMapper.deleteBatch(ids);
    }
    
    @Override
    public void increaseClicknum(Long id) {
        plantKnowledgeMapper.increaseClicknum(id);
    }
    
    @Override
    public void increaseStoreupnum(Long id) {
        plantKnowledgeMapper.increaseStoreupnum(id);
    }
    
    @Override
    public void decreaseStoreupnum(Long id) {
        plantKnowledgeMapper.decreaseStoreupnum(id);
    }
}
