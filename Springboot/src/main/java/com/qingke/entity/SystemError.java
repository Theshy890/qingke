package com.qingke.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
@TableName("system_error")
public class SystemError {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String module;

    private String errorType;

    private String errorMessage;

    private String stackTrace;

    private String requestUrl;

    private String requestMethod;

    private Long userId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
