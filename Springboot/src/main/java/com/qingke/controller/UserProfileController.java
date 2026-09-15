package com.qingke.controller;

import com.github.pagehelper.PageInfo;
import com.qingke.common.JwtUtil;
import com.qingke.common.OperationLogAnnotation;
import com.qingke.common.Result;
import com.qingke.entity.PlantMaintainRecord;
import com.qingke.entity.UserCollect;
import com.qingke.entity.UserProfileDTO;
import com.qingke.service.UserProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 用户主页控制器
 */
@RestController
@RequestMapping("/api/userProfile")
public class UserProfileController {

    @Autowired
    private UserProfileService userProfileService;

    /**
     * 获取用户主页信息
     */
    @GetMapping("/{userId}")
    @OperationLogAnnotation(module = "用户主页", type = "query", description = "查看用户主页")
    public Result<UserProfileDTO> getUserProfile(
            @PathVariable Long userId,
            HttpServletRequest request) {
        try {
            // 从token中获取当前登录用户ID
            Long currentUserId = getCurrentUserId(request);
            UserProfileDTO profile = userProfileService.getUserProfile(userId, currentUserId);
            return Result.success(profile);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取用户的养护记录（分页）
     */
    @GetMapping("/{userId}/maintainRecords")
    @OperationLogAnnotation(module = "用户主页", type = "query", description = "查看用户养护记录")
    public Result<PageInfo<PlantMaintainRecord>> getMaintainRecords(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpServletRequest request) {
        try {
            Long currentUserId = getCurrentUserId(request);
            PageInfo<PlantMaintainRecord> pageInfo = userProfileService.getMaintainRecords(userId, currentUserId, pageNum, pageSize);
            return Result.success(pageInfo);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取用户的收藏内容（分页）
     */
    @GetMapping("/{userId}/collections")
    @OperationLogAnnotation(module = "用户主页", type = "query", description = "查看用户收藏内容")
    public Result<PageInfo<UserCollect>> getCollections(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpServletRequest request) {
        try {
            Long currentUserId = getCurrentUserId(request);
            PageInfo<UserCollect> pageInfo = userProfileService.getCollections(userId, currentUserId, pageNum, pageSize);
            return Result.success(pageInfo);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 从请求中获取当前登录用户ID
     */
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
