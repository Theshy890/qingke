package com.qingke.entity;

import lombok.Data;

/**
 * 关注/粉丝列表项DTO，包含用户详细信息
 */
@Data
public class UserFollowListItemDTO {
    private Long id;
    private Long userId;       // 用户ID
    private String name;       // 用户昵称
    private String zh;         // 账号
    private String avatarUrl;  // 头像
    private String signature;  // 个性签名
    private Boolean isFollowing; // 当前用户是否已关注该用户
}
