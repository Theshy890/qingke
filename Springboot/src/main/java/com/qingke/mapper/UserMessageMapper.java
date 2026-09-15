package com.qingke.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qingke.entity.UserMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 私信 Mapper
 */
@Mapper
public interface UserMessageMapper extends BaseMapper<UserMessage> {

    /**
     * 查询未读私信数量
     */
    @Select("SELECT COUNT(*) FROM user_message WHERE receiver_id = #{userId} AND is_read = 0")
    Long countUnread(@Param("userId") Long userId);
}
