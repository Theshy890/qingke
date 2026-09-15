package com.qingke.entity;

import lombok.Data;

/**
 * 关注/粉丝统计 DTO
 */
@Data
public class UserFollowStatsDTO {
    private Long followingCount; // 关注数
    private Long followersCount; // 粉丝数
    private Long likesCount; // 获赞数
    private Boolean isFollowing; // 当前用户是否已关注该用户
}
