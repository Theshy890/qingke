package com.qingke.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.qingke.entity.SysUser;
import com.qingke.entity.UserVisitor;
import com.qingke.entity.VisitorDTO;
import com.qingke.mapper.SysUserMapper;
import com.qingke.mapper.UserVisitorMapper;
import com.qingke.service.UserVisitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 访客记录服务实现类
 */
@Service
public class UserVisitorServiceImpl implements UserVisitorService {

    @Autowired
    private UserVisitorMapper userVisitorMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Override
    public void recordVisit(Long userId, Long visitorId) {
        // 不记录自己访问自己
        if (userId.equals(visitorId)) {
            return;
        }

        // 检查是否已有记录（同一天内不重复记录）
        QueryWrapper<UserVisitor> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("visitor_id", visitorId);
        wrapper.apply("DATE(visit_time) = CURDATE()");

        if (userVisitorMapper.selectCount(wrapper) > 0) {
            return;
        }

        UserVisitor visitor = new UserVisitor();
        visitor.setUserId(userId);
        visitor.setVisitorId(visitorId);
        visitor.setIsRead(0);
        userVisitorMapper.insert(visitor);
    }

    @Override
    public PageInfo<VisitorDTO> getVisitorList(Long userId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        QueryWrapper<UserVisitor> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.orderByDesc("visit_time");

        List<UserVisitor> visitors = userVisitorMapper.selectList(wrapper);
        List<VisitorDTO> dtoList = visitors.stream().map(visitor -> {
            VisitorDTO dto = new VisitorDTO();
            dto.setId(visitor.getId());
            dto.setVisitorId(visitor.getVisitorId());
            dto.setVisitTime(visitor.getVisitTime());
            dto.setIsRead(visitor.getIsRead());

            // 查询访客信息
            SysUser visitorUser = sysUserMapper.selectById(visitor.getVisitorId());
            if (visitorUser != null) {
                dto.setVisitorNickname(visitorUser.getName());
                dto.setVisitorAvatar(visitorUser.getAvatarUrl());
            }

            return dto;
        }).collect(Collectors.toList());

        PageInfo<VisitorDTO> pageInfo = new PageInfo<>(dtoList);
        pageInfo.setTotal(new PageInfo<>(visitors).getTotal());
        return pageInfo;
    }

    @Override
    public void markAsRead(Long userId) {
        QueryWrapper<UserVisitor> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("is_read", 0);

        UserVisitor update = new UserVisitor();
        update.setIsRead(1);
        userVisitorMapper.update(update, wrapper);
    }

    @Override
    public Long getUnreadCount(Long userId) {
        return userVisitorMapper.countUnread(userId);
    }
}
