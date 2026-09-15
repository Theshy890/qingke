package com.qingke.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.qingke.entity.CommunityPost;
import com.qingke.mapper.CommunityCommentMapper;
import com.qingke.mapper.CommunityPostMapper;
import com.qingke.service.CommunityPostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class CommunityPostServiceImpl implements CommunityPostService {
    
    @Autowired
    private CommunityPostMapper communityPostMapper;
    
    @Autowired
    private CommunityCommentMapper communityCommentMapper;
    
    @Override
    public List<CommunityPost> findAll() { return communityPostMapper.findAll(); }
    
    @Override
    public PageInfo<CommunityPost> findByPage(Integer pageNum, Integer pageSize, String title, String userAccount, Integer auditStatus) {
        PageHelper.startPage(pageNum, pageSize);
        // 未指定审核状态时，默认只查已审核通过的帖子
        if (auditStatus == null) {
            auditStatus = 1;
        }
        List<CommunityPost> list = communityPostMapper.findByCondition(title, userAccount, auditStatus);
        return new PageInfo<>(list);
    }
    
    @Override
    public CommunityPost findById(Long id) { return communityPostMapper.findById(id); }
    
    @Override
    public List<CommunityPost> findByUserId(Long userId) { return communityPostMapper.findByUserId(userId); }
    
    @Override
    public void add(CommunityPost post) {
        post.setCreateTime(new Date());
        post.setPublishTime(new Date());
        post.setLikeCount(0);
        post.setDislikeCount(0);
        post.setCommentCount(0);
        if (post.getAuditStatus() == null) post.setAuditStatus(0);
        communityPostMapper.insert(post);
    }
    
    @Override
    public void update(CommunityPost post) { communityPostMapper.updateById(post); }
    
    @Override
    @Transactional
    public void deleteById(Long id) {
        communityCommentMapper.deleteByPostId(id);
        communityPostMapper.deleteById(id);
    }
    
    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {
        for (Long id : ids) { communityCommentMapper.deleteByPostId(id); }
        communityPostMapper.deleteBatch(ids);
    }
    
    @Override
    public void increaseLikeCount(Long id) { communityPostMapper.increaseLikeCount(id); }
    
    @Override
    public void decreaseLikeCount(Long id) { communityPostMapper.decreaseLikeCount(id); }
    
    @Override
    public void increaseDislikeCount(Long id) { communityPostMapper.increaseDislikeCount(id); }
    
    @Override
    public void updateAuditStatus(Long id, Integer auditStatus, String auditReply) {
        communityPostMapper.updateAuditStatus(id, auditStatus, auditReply);
    }
    
    @Override
    public void increaseCommentCount(Long id) { communityPostMapper.increaseCommentCount(id); }
    
    @Override
    public void decreaseCommentCount(Long id) { communityPostMapper.decreaseCommentCount(id); }
    
    @Override
    public void increaseCollectCount(Long id) { communityPostMapper.increaseCollectCount(id); }
    
    @Override
    public void decreaseCollectCount(Long id) { communityPostMapper.decreaseCollectCount(id); }
}
