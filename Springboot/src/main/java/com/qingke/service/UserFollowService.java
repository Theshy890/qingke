package com.qingke.service;

import com.github.pagehelper.PageInfo;
import com.qingke.entity.UserFollow;
import com.qingke.entity.UserFollowListItemDTO;
import com.qingke.entity.UserFollowStatsDTO;

/**
 * 用户关注服务接口
 */
public interface UserFollowService {

    /**
     * 关注用户
     */
    void follow(Long followerId, Long followingId);

    /**
     * 取消关注
     */
    void unfollow(Long followerId, Long followingId);

    /**
     * 检查是否已关注
     */
    Boolean isFollowing(Long followerId, Long followingId);

    /**
     * 获取关注列表（含用户详细信息）
     */
    PageInfo<UserFollowListItemDTO> getFollowingList(Long userId, Long currentUserId, int pageNum, int pageSize);

    /**
     * 获取粉丝列表（含用户详细信息）
     */
    PageInfo<UserFollowListItemDTO> getFollowersList(Long userId, Long currentUserId, int pageNum, int pageSize);

    /**
     * 获取关注/粉丝统计
     */
    UserFollowStatsDTO getStats(Long userId, Long currentUserId);
}
