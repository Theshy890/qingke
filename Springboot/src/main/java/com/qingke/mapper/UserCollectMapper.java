package com.qingke.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qingke.entity.UserCollect;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 收藏Mapper接口
 */
@Mapper
public interface UserCollectMapper extends BaseMapper<UserCollect> {
    
    /**
     * 根据用户ID和收藏对象ID查询收藏记录
     */
    UserCollect findByUseridAndSpid(@Param("userid") Long userid, @Param("spid") Long spid, @Param("tablename") String tablename);
    
    /**
     * 根据用户ID查询所有收藏
     */
    List<UserCollect> findByUserid(@Param("userid") Long userid);
    
    /**
     * 根据用户ID和表名查询收藏列表
     */
    List<UserCollect> findByUseridAndTablename(@Param("userid") Long userid, @Param("tablename") String tablename);
    
    /**
     * 删除收藏记录
     */
    void deleteByUseridAndSpid(@Param("userid") Long userid, @Param("spid") Long spid, @Param("tablename") String tablename);
}
