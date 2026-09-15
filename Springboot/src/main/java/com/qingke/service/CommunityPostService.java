package com.qingke.service;

import com.qingke.entity.CommunityPost;
import com.github.pagehelper.PageInfo;
import java.util.List;

public interface CommunityPostService {
    List<CommunityPost> findAll();
    PageInfo<CommunityPost> findByPage(Integer pageNum, Integer pageSize, String title, String userAccount, Integer auditStatus);
    CommunityPost findById(Long id);
    List<CommunityPost> findByUserId(Long userId);
    void add(CommunityPost post);
    void update(CommunityPost post);
    void deleteById(Long id);
    void deleteBatch(List<Long> ids);
    void increaseLikeCount(Long id);
    void decreaseLikeCount(Long id);
    void increaseDislikeCount(Long id);
    void updateAuditStatus(Long id, Integer auditStatus, String auditReply);
    void increaseCommentCount(Long id);
    void decreaseCommentCount(Long id);
    void increaseCollectCount(Long id);
    void decreaseCollectCount(Long id);
}
