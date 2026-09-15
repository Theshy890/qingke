package com.qingke.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qingke.entity.PlantMaintainRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 养护记录 Mapper
 */
@Mapper
public interface PlantMaintainRecordMapper extends BaseMapper<PlantMaintainRecord> {
    
    @Select("SELECT * FROM plant_maintain_record ORDER BY create_time DESC")
    List<PlantMaintainRecord> findAll();
    
    @Select("SELECT * FROM plant_maintain_record WHERE user_account = #{userAccount} ORDER BY create_time DESC")
    List<PlantMaintainRecord> findByZhanghao(@Param("userAccount") String userAccount);
    
    @Select("SELECT * FROM plant_maintain_record WHERE id = #{id}")
    PlantMaintainRecord findById(@Param("id") Long id);
    
    @Select("SELECT * FROM plant_maintain_record WHERE plant_name = #{plantName} AND user_account = #{userAccount} ORDER BY create_time DESC")
    List<PlantMaintainRecord> findByPlantAndUser(@Param("plantName") String plantName, @Param("userAccount") String userAccount);
}