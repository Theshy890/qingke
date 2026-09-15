package com.qingke.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qingke.entity.PlantKnowledge;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 养护知识Mapper接口
 */
@Mapper
public interface PlantKnowledgeMapper extends BaseMapper<PlantKnowledge> {
    
    /**
     * 查询所有养护知识
     */
    List<PlantKnowledge> findAll();
    
    /**
     * 根据条件查询养护知识
     */
    List<PlantKnowledge> findByCondition(
        @Param("plantName") String plantName, 
        @Param("categoryName") String categoryName,
        @Param("orderBy") String orderBy
    );
    
    /**
     * 根据ID查询养护知识
     */
    PlantKnowledge findById(@Param("id") Long id);
    
    /**
     * 批量删除养护知识
     */
    void deleteBatch(@Param("ids") List<Long> ids);
    
    /**
     * 增加点击次数
     */
    void increaseClicknum(@Param("id") Long id);
    
    /**
     * 增加收藏数量
     */
    void increaseStoreupnum(@Param("id") Long id);
    
    /**
     * 减少收藏数量
     */
    void decreaseStoreupnum(@Param("id") Long id);
}
