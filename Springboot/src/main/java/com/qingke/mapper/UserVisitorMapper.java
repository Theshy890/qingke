package com.qingke.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qingke.entity.UserVisitor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 访客记录 Mapper
 */
@Mapper
public interface UserVisitorMapper extends BaseMapper<UserVisitor> {

    /**
     * 查询未读访客数量
     */
    @Select("SELECT COUNT(*) FROM user_visitor WHERE user_id = #{userId} AND is_read = 0")
    Long countUnread(@Param("userId") Long userId);
}
