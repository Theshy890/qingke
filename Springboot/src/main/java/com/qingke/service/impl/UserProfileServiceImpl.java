package com.qingke.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.qingke.common.BusinessException;
import com.qingke.entity.PlantMaintainRecord;
import com.qingke.entity.SysUser;
import com.qingke.entity.UserCollect;
import com.qingke.entity.UserProfileDTO;
import com.qingke.mapper.PlantMaintainRecordMapper;
import com.qingke.mapper.SysUserMapper;
import com.qingke.mapper.UserCollectMapper;
import com.qingke.service.UserProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 用户主页服务实现类
 */
@Service
public class UserProfileServiceImpl implements UserProfileService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private PlantMaintainRecordMapper plantMaintainRecordMapper;

    @Autowired
    private UserCollectMapper userCollectMapper;

    @Override
    public UserProfileDTO getUserProfile(Long targetUserId, Long currentUserId) {
        // 1. 查询目标用户
        SysUser targetUser = sysUserMapper.selectById(targetUserId);
        if (targetUser == null) {
            throw new BusinessException("用户不存在");
        }

        // 2. 检查是否是本人
        boolean isSelf = targetUserId.equals(currentUserId);

        // 3. 检查个人资料可见性
        if (!isSelf && "private".equals(targetUser.getProfileVisibility())) {
            throw new BusinessException("该用户设置了隐私保护，无法查看");
        }

        // 4. 构建返回数据
        UserProfileDTO dto = new UserProfileDTO();
        dto.setId(targetUser.getId());
        dto.setName(targetUser.getName());
        dto.setAvatarUrl(targetUser.getAvatarUrl());
        dto.setSignature(targetUser.getSignature());
        dto.setGender(targetUser.getGender());
        dto.setRegisterTime(targetUser.getRegisterTime());
        dto.setPoints(targetUser.getPoints());

        // 5. 设置隐私可见性
        dto.setCareRecordsVisible(isSelf || (targetUser.getCareRecordsPublic() != null && targetUser.getCareRecordsPublic() == 1));
        dto.setFavoritesVisible(isSelf || (targetUser.getFavoritesPublic() != null && targetUser.getFavoritesPublic() == 1));

        // 6. 统计数据
        QueryWrapper<PlantMaintainRecord> maintainWrapper = new QueryWrapper<>();
        maintainWrapper.eq("user_id", targetUserId);
        Long maintainCount = plantMaintainRecordMapper.selectCount(maintainWrapper);
        dto.setMaintainRecordCount(maintainCount);

        QueryWrapper<UserCollect> collectWrapper = new QueryWrapper<>();
        collectWrapper.eq("user_id", targetUserId);
        Long collectCount = userCollectMapper.selectCount(collectWrapper);
        dto.setCollectCount(collectCount);

        return dto;
    }

    @Override
    public PageInfo<PlantMaintainRecord> getMaintainRecords(Long targetUserId, Long currentUserId, int pageNum, int pageSize) {
        // 1. 查询目标用户
        SysUser targetUser = sysUserMapper.selectById(targetUserId);
        if (targetUser == null) {
            throw new BusinessException("用户不存在");
        }

        // 2. 检查是否是本人
        boolean isSelf = targetUserId.equals(currentUserId);

        // 3. 检查养护记录可见性
        if (!isSelf && (targetUser.getCareRecordsPublic() == null || targetUser.getCareRecordsPublic() == 0)) {
            throw new BusinessException("该用户未公开养护记录");
        }

        // 4. 查询养护记录
        PageHelper.startPage(pageNum, pageSize);
        QueryWrapper<PlantMaintainRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", targetUserId);
        wrapper.orderByDesc("create_time");
        return new PageInfo<>(plantMaintainRecordMapper.selectList(wrapper));
    }

    @Override
    public PageInfo<UserCollect> getCollections(Long targetUserId, Long currentUserId, int pageNum, int pageSize) {
        // 1. 查询目标用户
        SysUser targetUser = sysUserMapper.selectById(targetUserId);
        if (targetUser == null) {
            throw new BusinessException("用户不存在");
        }

        // 2. 检查是否是本人
        boolean isSelf = targetUserId.equals(currentUserId);

        // 3. 检查收藏可见性
        if (!isSelf && (targetUser.getFavoritesPublic() == null || targetUser.getFavoritesPublic() == 0)) {
            throw new BusinessException("该用户未公开收藏内容");
        }

        // 4. 查询收藏内容
        PageHelper.startPage(pageNum, pageSize);
        QueryWrapper<UserCollect> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", targetUserId);
        wrapper.orderByDesc("create_time");
        return new PageInfo<>(userCollectMapper.selectList(wrapper));
    }
}
