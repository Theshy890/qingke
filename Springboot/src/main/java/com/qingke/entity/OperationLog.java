package com.qingke.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 操作日志实体类
 */
@Data
@TableName("operation_log")
public class OperationLog implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 操作用户
     */
    private String username;
    
    /**
     * 用户角色
     */
    private String role;
    
    /**
     * 操作类型：login-登录, logout-登出, add-添加, update-修改, delete-删除, audit-审核
     */
    private String operationType;
    
    /**
     * 操作模块：user-用户, knowledge-养护知识, record-养护记录, community-社区互动等
     */
    private String module;
    
    /**
     * 操作描述
     */
    private String description;
    
    /**
     * IP地址
     */
    private String ipAddress;
    
    /**
     * 操作时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date operationTime;
    
    /**
     * 操作结果：success-成功, fail-失败
     */
    private String result;

    /**
     * 错误详情（失败时记录异常堆栈或详细信息）
     */
    private String errorMessage;
}