package com.qingke.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.qingke.entity.AiRecognitionRequest;
import com.qingke.entity.AiRecognitionResponse;
import com.qingke.entity.PlantRecognize;
import com.qingke.mapper.PlantRecognizeMapper;
import com.qingke.service.ZhipuAiService;
import com.qingke.service.PlantRecognizeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 绿植识别服务实现类（智谱 GLM 多模态大模型版本）
 */
@Slf4j
@Service
public class PlantRecognizeServiceImpl implements PlantRecognizeService {
    
    @Autowired
    private PlantRecognizeMapper plantRecognizeMapper;
    
    @Autowired
    private ZhipuAiService zhipuAiService;
    
    @Override
    public AiRecognitionResponse recognizePlant(AiRecognitionRequest request) {
        try {
            log.info("开始识别植物，用户ID：{}", request.getUserid());
            
            // 调用智谱 GLM 多模态大模型
            AiRecognitionResponse response = zhipuAiService.recognizePlantDisease(request.getImage());
            
            log.info("识别完成，植物名称：{}，是否有病害：{}", response.getPlantName(), response.getHasDisease());
            
            return response;
            
        } catch (Exception e) {
            log.error("植物识别失败", e);
            throw new RuntimeException("AI识别失败：" + e.getMessage());
        }
    }
    
    @Override
    public void saveRecord(PlantRecognize record) {
        record.setCreateTime(new Date());
        plantRecognizeMapper.insert(record);
    }
    
    @Override
    public List<PlantRecognize> findByUserid(Long userid) {
        return plantRecognizeMapper.findByUserid(userid);
    }
    
    @Override
    public PageInfo<PlantRecognize> findByPage(Integer pageNum, Integer pageSize, Long userid) {
        PageHelper.startPage(pageNum, pageSize);
        List<PlantRecognize> list = plantRecognizeMapper.findByUserid(userid);
        return new PageInfo<>(list);
    }
    
    @Override
    public PlantRecognize findById(Long id) {
        return plantRecognizeMapper.findById(id);
    }
    
    @Override
    public void deleteById(Long id) {
        plantRecognizeMapper.deleteById(id);
    }
    
    @Override
    public void deleteBatch(List<Long> ids) {
        plantRecognizeMapper.deleteBatch(ids);
    }
    
    @Override
    public List<PlantRecognize> findAll() {
        return plantRecognizeMapper.findAll();
    }
}
