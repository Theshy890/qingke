package com.qingke.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qingke.entity.CommentReplyDTO;
import com.qingke.entity.CommentTreeDTO;
import com.qingke.entity.CommunityComment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommunityCommentMapper extends BaseMapper<CommunityComment> {
    List<CommunityComment> findByPostId(@Param("postId") Long postId);
    CommunityComment findById(@Param("id") Long id);
    void deleteBatch(@Param("ids") List<Long> ids);
    void deleteByPostId(@Param("postId") Long postId);
    void increaseLikeCount(@Param("id") Long id);
    void increaseDislikeCount(@Param("id") Long id);
    void updateReply(@Param("id") Long id, @Param("reply") String reply);

    // 查询主评论列表（包含回复数量统计）
    List<CommentTreeDTO> findMainCommentsByPostId(@Param("postId") Long postId);

    // 查询某个主评论下的所有回复
    List<CommentReplyDTO> findRepliesByParentId(@Param("parentId") Long parentId);

    // 分页查询某个主评论下的回复
    List<CommentReplyDTO> findRepliesByParentIdWithPage(
        @Param("parentId") Long parentId,
        @Param("offset") Integer offset,
        @Param("pageSize") Integer pageSize
    );
}
