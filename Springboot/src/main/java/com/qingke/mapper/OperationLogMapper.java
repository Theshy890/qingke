package com.qingke.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qingke.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {
    
    /**
     * 查询最近的日志记录
     */
    @Select("SELECT * FROM operation_log ORDER BY operation_time DESC LIMIT #{limit}")
    List<OperationLog> findRecentLogs(int limit);
    
    /**
     * 根据用户查询日志
     */
    @Select("SELECT * FROM operation_log WHERE username = #{username} ORDER BY operation_time DESC")
    List<OperationLog> findByUsername(String username);
}
