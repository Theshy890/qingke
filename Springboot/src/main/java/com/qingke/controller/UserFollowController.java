package com.qingke.controller;

import com.github.pagehelper.PageInfo;
import com.qingke.common.JwtUtil;
import com.qingke.common.Result;
import com.qingke.entity.UserFollowListItemDTO;
import com.qingke.entity.UserFollowStatsDTO;
import com.qingke.service.UserFollowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 用户关注控制器
 */
@RestController
@RequestMapping("/api/userFollow")
public class UserFollowController {

    @Autowired
    private UserFollowService userFollowService;

    /**
     * 关注用户
     */
    @PostMapping("/follow")
    public Result<?> follow(@RequestParam Long followerId, @RequestParam Long followingId) {
        userFollowService.follow(followerId, followingId);
        return Result.success("关注成功");
    }

    /**
     * 取消关注
     */
    @DeleteMapping("/unfollow")
    public Result<?> unfollow(@RequestParam Long followerId, @RequestParam Long followingId) {
        userFollowService.unfollow(followerId, followingId);
        return Result.success("取消关注成功");
    }

    /**
     * 检查是否已关注
     */
    @GetMapping("/isFollowing")
    public Result<Boolean> isFollowing(@RequestParam Long followerId, @RequestParam Long followingId) {
        Boolean isFollowing = userFollowService.isFollowing(followerId, followingId);
        return Result.success(isFollowing);
    }

    /**
     * 获取关注列表（含用户详细信息）
     */
    @GetMapping("/following/{userId}")
    public Result<PageInfo<UserFollowListItemDTO>> getFollowingList(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        PageInfo<UserFollowListItemDTO> pageInfo = userFollowService.getFollowingList(userId, currentUserId, pageNum, pageSize);
        return Result.success(pageInfo);
    }

    /**
     * 获取粉丝列表（含用户详细信息）
     */
    @GetMapping("/followers/{userId}")
    public Result<PageInfo<UserFollowListItemDTO>> getFollowersList(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpServletRequest request) {
        Long currentUserId = getCurrentUserId(request);
        PageInfo<UserFollowListItemDTO> pageInfo = userFollowService.getFollowersList(userId, currentUserId, pageNum, pageSize);
        return Result.success(pageInfo);
    }

    /**
     * 获取关注/粉丝统计
     */
    @GetMapping("/stats/{userId}")
    public Result<UserFollowStatsDTO> getStats(@PathVariable Long userId, @RequestParam(required = false) Long currentUserId) {
        UserFollowStatsDTO stats = userFollowService.getStats(userId, currentUserId);
        return Result.success(stats);
    }

    private Long getCurrentUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        if (token == null || token.isEmpty()) {
            return null;
        }
        try {
            return JwtUtil.getUserIdFromToken(token);
        } catch (Exception e) {
            return null;
        }
    }
}
