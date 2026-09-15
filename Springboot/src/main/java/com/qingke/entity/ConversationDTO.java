package com.qingke.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 会话列表项 DTO
 */
@Data
public class ConversationDTO {
    private Long userId; // 对话用户ID
    private String nickname; // 对话用户昵称
    private String avatarUrl; // 对话用户头像
    private String lastMessage; // 最后一条消息内容
    private Integer unreadCount; // 未读消息数

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lastMessageTime; // 最后消息时间
}
