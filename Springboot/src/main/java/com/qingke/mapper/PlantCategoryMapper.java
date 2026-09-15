package com.qingke.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qingke.entity.PlantCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 绿植种类Mapper接口
 */
@Mapper
public interface PlantCategoryMapper extends BaseMapper<PlantCategory> {
    
    /**
     * 查询所有绿植种类
     */
    List<PlantCategory> selectAll();
    
    /**
     * 根据名称模糊查询
     */
    List<PlantCategory> selectByName(@Param("categoryName") String categoryName);
    
    /**
     * 分页查询绿植种类
     */
    List<PlantCategory> selectByPage(@Param("categoryName") String categoryName);
    
    /**
     * 批量删除绿植种类
     */
    int deleteBatch(@Param("ids") List<Long> ids);
    
    /**
     * 检查绿植种类名称是否存在
     */
    int checkNameExists(@Param("categoryName") String categoryName, @Param("id") Long id);
}
