package com.qingke.service;

import com.qingke.entity.UserCollect;

import java.util.List;

/**
 * 收藏服务接口
 */
public interface UserCollectService {
    
    /**
     * 添加收藏
     */
    void add(UserCollect userCollect);
    
    /**
     * 取消收藏
     */
    void cancel(Long userid, Long spid, String tablename);
    
    /**
     * 检查是否已收藏
     */
    boolean isStored(Long userid, Long spid, String tablename);
    
    /**
     * 根据用户ID查询所有收藏
     */
    List<UserCollect> findByUserid(Long userid);
    
    /**
     * 根据用户ID和表名查询收藏列表
     */
    List<UserCollect> findByUseridAndTablename(Long userid, String tablename);
}
