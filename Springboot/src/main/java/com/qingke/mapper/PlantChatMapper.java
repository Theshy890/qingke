package com.qingke.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qingke.entity.PlantChat;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * AI聊天Mapper接口
 */
@Mapper
public interface PlantChatMapper extends BaseMapper<PlantChat> {

    /**
     * 根据用户ID查询聊天记录
     */
    List<PlantChat> findByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID删除聊天记录
     */
    void deleteByUserId(@Param("userId") Long userId);
}
