package com.qingke.service.impl;

import com.qingke.entity.UserCollect;
import com.qingke.mapper.UserCollectMapper;
import com.qingke.service.UserCollectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 收藏服务实现类
 */
@Service
public class UserCollectServiceImpl implements UserCollectService {
    
    @Autowired
    private UserCollectMapper userCollectMapper;
    
    @Override
    public void add(UserCollect userCollect) {
        userCollect.setCreateTime(new Date());
        userCollect.setTargetType("1"); // 1-收藏
        userCollectMapper.insert(userCollect);
    }
    
    @Override
    public void cancel(Long userid, Long spid, String tablename) {
        userCollectMapper.deleteByUseridAndSpid(userid, spid, tablename);
    }
    
    @Override
    public boolean isStored(Long userid, Long spid, String tablename) {
        UserCollect userCollect = userCollectMapper.findByUseridAndSpid(userid, spid, tablename);
        return userCollect != null;
    }
    
    @Override
    public List<UserCollect> findByUserid(Long userid) {
        return userCollectMapper.findByUserid(userid);
    }
    
    @Override
    public List<UserCollect> findByUseridAndTablename(Long userid, String tablename) {
        return userCollectMapper.findByUseridAndTablename(userid, tablename);
    }
}
