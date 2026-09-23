package com.qingke.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.qingke.entity.CommunityComment;
import com.qingke.entity.CommunityPost;
import com.qingke.entity.PlantChat;
import com.qingke.entity.SysUser;
import com.qingke.mapper.CommunityCommentMapper;
import com.qingke.mapper.CommunityPostMapper;
import com.qingke.mapper.PlantChatMapper;
import com.qingke.mapper.SysUserMapper;
import com.qingke.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysUserServiceImpl implements SysUserService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private CommunityPostMapper communityPostMapper;

    @Autowired
    private CommunityCommentMapper communityCommentMapper;

    @Autowired
    private PlantChatMapper plantChatMapper;

    @Override
    public SysUser findByZh(String zh) { return sysUserMapper.findByZh(zh); }

    @Override
    public SysUser findByPhone(String phone) { return sysUserMapper.findByPhone(phone); }

    @Override
    public void increasePasswordWrongNum(Long id) { sysUserMapper.increasePasswordWrongNum(id); }

    @Override
    public void resetPasswordWrongNum(Long id) { sysUserMapper.resetPasswordWrongNum(id); }

    @Override
    public void updatePassword(Long id, String password) { sysUserMapper.updatePassword(id, password); }

    @Override
    public List<SysUser> findAll() { return sysUserMapper.findAll(); }

    @Override
    public PageInfo<SysUser> findByPage(Integer pageNum, Integer pageSize, String zh, String name, String role) {
        PageHelper.startPage(pageNum, pageSize);
        List<SysUser> list = sysUserMapper.findByCondition(zh, name, role);
        return new PageInfo<>(list);
    }

    @Override
    public SysUser findById(Long id) { return sysUserMapper.findById(id); }

    @Override
    public void add(SysUser user) { sysUserMapper.insert(user); }

    @Override
    public void update(SysUser user) {
        sysUserMapper.updateById(user);
        // 头像/昵称变更后同步冗余存储的快照数据（帖子/评论/AI聊天记录），保证全站即时一致
        if (user.getId() != null) {
            SysUser fresh = sysUserMapper.findById(user.getId());
            if (fresh != null) {
                syncUserSnapshot(fresh);
            }
        }
    }

    /**
     * 将用户最新的头像与昵称同步到冗余存储的业务表
     */
    private void syncUserSnapshot(SysUser user) {
        if (user.getAvatarUrl() != null) {
            UpdateWrapper<CommunityPost> postWrapper = new UpdateWrapper<>();
            postWrapper.eq("user_id", user.getId()).set("avatar_url", user.getAvatarUrl());
            communityPostMapper.update(null, postWrapper);

            UpdateWrapper<CommunityComment> commentWrapper = new UpdateWrapper<>();
            commentWrapper.eq("user_id", user.getId()).set("avatar_url", user.getAvatarUrl());
            communityCommentMapper.update(null, commentWrapper);

            UpdateWrapper<PlantChat> chatWrapper = new UpdateWrapper<>();
            chatWrapper.eq("user_id", user.getId()).set("avatar_url", user.getAvatarUrl());
            plantChatMapper.update(null, chatWrapper);
        }

        if (user.getName() != null) {
            UpdateWrapper<CommunityPost> postWrapper = new UpdateWrapper<>();
            postWrapper.eq("user_id", user.getId()).set("user_account", user.getName());
            communityPostMapper.update(null, postWrapper);

            UpdateWrapper<CommunityComment> commentWrapper = new UpdateWrapper<>();
            commentWrapper.eq("user_id", user.getId()).set("nickname", user.getName());
            communityCommentMapper.update(null, commentWrapper);

            UpdateWrapper<PlantChat> chatWrapper = new UpdateWrapper<>();
            chatWrapper.eq("user_id", user.getId()).set("user_name", user.getName());
            plantChatMapper.update(null, chatWrapper);
        }
    }

    @Override
    public void deleteById(Long id) { sysUserMapper.deleteById(id); }

    @Override
    public void deleteBatch(List<Long> ids) { sysUserMapper.deleteBatch(ids); }

    @Override
    public void updateStatus(Long id, Integer status) { sysUserMapper.updateStatus(id, status); }

    @Override
    public void updateLastLoginTime(Long id) { sysUserMapper.updateLastLoginTime(id); }

    @Override
    public void addPoints(Long id, int delta) { sysUserMapper.addPoints(id, delta); }

    @Override
    public boolean deductPoints(Long id, int cost) { return sysUserMapper.deductPoints(id, cost) > 0; }
}
