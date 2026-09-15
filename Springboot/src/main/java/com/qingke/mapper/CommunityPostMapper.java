package com.qingke.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qingke.entity.CommunityPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommunityPostMapper extends BaseMapper<CommunityPost> {
    List<CommunityPost> findAll();
    List<CommunityPost> findByCondition(@Param("title") String title, @Param("userAccount") String userAccount, @Param("auditStatus") Integer auditStatus);
    CommunityPost findById(@Param("id") Long id);
    List<CommunityPost> findByUserId(@Param("userId") Long userId);
    void deleteBatch(@Param("ids") List<Long> ids);
    void increaseLikeCount(@Param("id") Long id);
    void decreaseLikeCount(@Param("id") Long id);
    void increaseDislikeCount(@Param("id") Long id);
    void increaseCommentCount(@Param("id") Long id);
    void decreaseCommentCount(@Param("id") Long id);
    void updateAuditStatus(@Param("id") Long id, @Param("auditStatus") Integer auditStatus, @Param("auditReply") String auditReply);
    void increaseCollectCount(@Param("id") Long id);
    void decreaseCollectCount(@Param("id") Long id);
}
