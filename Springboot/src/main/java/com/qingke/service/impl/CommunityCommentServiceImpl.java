package com.qingke.service.impl;

import com.qingke.entity.CommentReplyDTO;
import com.qingke.entity.CommentTreeDTO;
import com.qingke.entity.CommunityComment;
import com.qingke.mapper.CommunityCommentMapper;
import com.qingke.mapper.CommunityPostMapper;
import com.qingke.service.CommunityCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class CommunityCommentServiceImpl implements CommunityCommentService {

    @Autowired
    private CommunityCommentMapper communityCommentMapper;

    @Autowired
    private CommunityPostMapper communityPostMapper;

    @Override
    public List<CommunityComment> findByPostId(Long postId) {
        return communityCommentMapper.findByPostId(postId);
    }

    @Override
    public CommunityComment findById(Long id) {
        return communityCommentMapper.findById(id);
    }

    @Override
    @Transactional
    public void add(CommunityComment comment) {
        comment.setCreateTime(new Date());
        comment.setLikeCount(0);
        comment.setDislikeCount(0);
        comment.setIsTop(0);
        communityCommentMapper.insert(comment);
        communityPostMapper.increaseCommentCount(comment.getPostId());
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        CommunityComment comment = communityCommentMapper.findById(id);
        if (comment != null) {
            communityCommentMapper.deleteById(id);
            communityPostMapper.decreaseCommentCount(comment.getPostId());
        }
    }

    @Override
    public void deleteBatch(List<Long> ids) {
        communityCommentMapper.deleteBatch(ids);
    }

    @Override
    @Transactional
    public void deleteByPostId(Long postId) {
        communityCommentMapper.deleteByPostId(postId);
    }

    @Override
    public void increaseLikeCount(Long id) {
        communityCommentMapper.increaseLikeCount(id);
    }

    @Override
    public void increaseDislikeCount(Long id) {
        communityCommentMapper.increaseDislikeCount(id);
    }

    @Override
    public void updateReply(Long id, String reply) {
        communityCommentMapper.updateReply(id, reply);
    }

    @Override
    public List<CommentTreeDTO> getCommentTree(Long postId) {
        // 查询主评论
        List<CommentTreeDTO> mainComments = communityCommentMapper.findMainCommentsByPostId(postId);

        // 为每个主评论加载回复列表
        for (CommentTreeDTO comment : mainComments) {
            List<CommentReplyDTO> replies = communityCommentMapper.findRepliesByParentId(comment.getId());
            comment.setReplies(replies);
        }

        return mainComments;
    }

    @Override
    @Transactional
    public void replyComment(CommunityComment comment) {
        comment.setCreateTime(new Date());
        comment.setLikeCount(0);
        comment.setDislikeCount(0);
        comment.setIsTop(0);
        communityCommentMapper.insert(comment);
        communityPostMapper.increaseCommentCount(comment.getPostId());
    }

    @Override
    public List<CommentReplyDTO> getRepliesByParentId(Long parentId, Integer pageNum, Integer pageSize) {
        Integer offset = (pageNum - 1) * pageSize;
        return communityCommentMapper.findRepliesByParentIdWithPage(parentId, offset, pageSize);
    }
}
