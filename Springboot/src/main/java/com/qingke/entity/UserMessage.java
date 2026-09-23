package com.qingke.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 私信实体类
 */
@Data
@TableName("user_message")
public class UserMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long senderId; // 发送者ID
    private Long receiverId; // 接收者ID
    private String content; // 消息内容
    private String quoteContent; // 引用消息内容快照
    private Integer isRecalled; // 是否撤回：0-否，1-是
    private Integer senderDeleted; // 发送者已删除：0-否，1-是
    private Integer receiverDeleted; // 接收者已删除：0-否，1-是
    private Integer isRead; // 是否已读：0-未读，1-已读

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime; // 发送时间
}
