package com.qingke.entity;

import lombok.Data;

/**
 * 消息中心未读数统计 DTO
 */
@Data
public class MessageCenterDTO {
    private Long unreadMessages; // 未读私信数
    private Long unreadVisitors; // 未读访客数
    private Long unreadFollowers; // 新增粉丝数（最近7天）
    private Long total; // 总未读数
}
