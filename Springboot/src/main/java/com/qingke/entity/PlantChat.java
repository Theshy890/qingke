package com.qingke.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * AI聊天智能助手表
 */
@Data
@TableName("plant_chat")
public class PlantChat implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 管理员ID
     */
    private Long adminId;
    
    /**
     * 用户提问
     */
    private String ask;
    
    /**
     * AI回复
     */
    private String reply;
    
    /**
     * 是否已回复：0-未回复，1-已回复
     */
    private Integer isReplied;
    
    /**
     * 是否已读：0-未读，1-已读
     */
    private Integer isRead;
    
    /**
     * 用户头像
     */
    private String avatarUrl;
    
    /**
     * 用户名
     */
    private String userName;
    
    /**
     * 消息类型
     */
    private String msgType;
    
    /**
     * 图片URL（base64或图片路径，多模态对话时使用）
     */
    private String imageUrl;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
