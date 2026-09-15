package com.qingke.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qingke.entity.PlantRecognize;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 绿植识别Mapper接口
 */
@Mapper
public interface PlantRecognizeMapper extends BaseMapper<PlantRecognize> {
    
    /**
     * 查询所有识别记录
     */
    List<PlantRecognize> findAll();
    
    /**
     * 根据用户ID查询识别记录
     */
    List<PlantRecognize> findByUserid(@Param("userid") Long userid);
    
    /**
     * 根据ID查询识别记录
     */
    PlantRecognize findById(@Param("id") Long id);
    
    /**
     * 批量删除识别记录
     */
    void deleteBatch(@Param("ids") List<Long> ids);
}
