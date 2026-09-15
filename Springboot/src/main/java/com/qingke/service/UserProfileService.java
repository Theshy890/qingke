package com.qingke.service;

import com.github.pagehelper.PageInfo;
import com.qingke.entity.PlantMaintainRecord;
import com.qingke.entity.UserCollect;
import com.qingke.entity.UserProfileDTO;

/**
 * 用户主页服务接口
 */
public interface UserProfileService {

    /**
     * 获取用户主页信息
     * @param targetUserId 目标用户ID
     * @param currentUserId 当前登录用户ID
     * @return 用户主页信息
     */
    UserProfileDTO getUserProfile(Long targetUserId, Long currentUserId);

    /**
     * 获取用户的养护记录（分页）
     * @param targetUserId 目标用户ID
     * @param currentUserId 当前登录用户ID
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 养护记录分页数据
     */
    PageInfo<PlantMaintainRecord> getMaintainRecords(Long targetUserId, Long currentUserId, int pageNum, int pageSize);

    /**
     * 获取用户的收藏内容（分页）
     * @param targetUserId 目标用户ID
     * @param currentUserId 当前登录用户ID
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 收藏内容分页数据
     */
    PageInfo<UserCollect> getCollections(Long targetUserId, Long currentUserId, int pageNum, int pageSize);
}
