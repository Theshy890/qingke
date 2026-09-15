package com.qingke.service;

import com.qingke.entity.CommentReplyDTO;
import com.qingke.entity.CommentTreeDTO;
import com.qingke.entity.CommunityComment;
import java.util.List;

public interface CommunityCommentService {
    List<CommunityComment> findByPostId(Long postId);
    CommunityComment findById(Long id);
    void add(CommunityComment comment);
    void deleteById(Long id);
    void deleteBatch(List<Long> ids);
    void deleteByPostId(Long postId);
    void increaseLikeCount(Long id);
    void increaseDislikeCount(Long id);
    void updateReply(Long id, String reply);

    // 获取评论树（主评论+回复列表）
    List<CommentTreeDTO> getCommentTree(Long postId);

    // 回复评论（用户回复某条评论）
    void replyComment(CommunityComment comment);

    // 获取某个主评论下的回复列表（分页）
    List<CommentReplyDTO> getRepliesByParentId(Long parentId, Integer pageNum, Integer pageSize);
}
