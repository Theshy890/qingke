package com.qingke.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.qingke.common.BusinessException;
import com.qingke.entity.UserFollow;
import com.qingke.entity.UserFollowListItemDTO;
import com.qingke.entity.UserFollowStatsDTO;
import com.qingke.mapper.UserFollowMapper;
import com.qingke.service.UserFollowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户关注服务实现类
 */
@Service
public class UserFollowServiceImpl implements UserFollowService {

    @Autowired
    private UserFollowMapper userFollowMapper;

    @Override
    public void follow(Long followerId, Long followingId) {
        if (followerId.equals(followingId)) {
            throw new BusinessException("不能关注自己");
        }

        // 检查是否已关注
        if (isFollowing(followerId, followingId)) {
            throw new BusinessException("已经关注过了");
        }

        UserFollow userFollow = new UserFollow();
        userFollow.setFollowerId(followerId);
        userFollow.setFollowingId(followingId);
        userFollowMapper.insert(userFollow);
    }

    @Override
    public void unfollow(Long followerId, Long followingId) {
        QueryWrapper<UserFollow> wrapper = new QueryWrapper<>();
        wrapper.eq("follower_id", followerId);
        wrapper.eq("following_id", followingId);
        userFollowMapper.delete(wrapper);
    }

    @Override
    public Boolean isFollowing(Long followerId, Long followingId) {
        if (followerId == null || followingId == null) {
            return false;
        }
        return userFollowMapper.isFollowing(followerId, followingId);
    }

    @Override
    public PageInfo<UserFollowListItemDTO> getFollowingList(Long userId, Long currentUserId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<UserFollowListItemDTO> list = userFollowMapper.getFollowingListWithUser(userId, currentUserId);
        return new PageInfo<>(list);
    }

    @Override
    public PageInfo<UserFollowListItemDTO> getFollowersList(Long userId, Long currentUserId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<UserFollowListItemDTO> list = userFollowMapper.getFollowersListWithUser(userId, currentUserId);
        return new PageInfo<>(list);
    }

    @Override
    public UserFollowStatsDTO getStats(Long userId, Long currentUserId) {
        UserFollowStatsDTO stats = new UserFollowStatsDTO();
        stats.setFollowingCount(userFollowMapper.countFollowing(userId));
        stats.setFollowersCount(userFollowMapper.countFollowers(userId));
        stats.setLikesCount(userFollowMapper.countLikes(userId));

        if (currentUserId != null && !currentUserId.equals(userId)) {
            stats.setIsFollowing(isFollowing(currentUserId, userId));
        } else {
            stats.setIsFollowing(false);
        }

        return stats;
    }
}
