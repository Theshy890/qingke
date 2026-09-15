package com.qingke.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qingke.entity.UserFollow;
import com.qingke.entity.UserFollowListItemDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户关注 Mapper
 */
@Mapper
public interface UserFollowMapper extends BaseMapper<UserFollow> {

    /**
     * 查询关注数
     */
    @Select("SELECT COUNT(*) FROM user_follow WHERE follower_id = #{userId}")
    Long countFollowing(@Param("userId") Long userId);

    /**
     * 查询粉丝数
     */
    @Select("SELECT COUNT(*) FROM user_follow WHERE following_id = #{userId}")
    Long countFollowers(@Param("userId") Long userId);

    /**
     * 检查是否已关注
     */
    @Select("SELECT COUNT(*) > 0 FROM user_follow WHERE follower_id = #{followerId} AND following_id = #{followingId}")
    Boolean isFollowing(@Param("followerId") Long followerId, @Param("followingId") Long followingId);

    /**
     * 查询用户获赞数（社区帖子点赞数总和）
     */
    @Select("SELECT COALESCE(SUM(like_count), 0) FROM community_post WHERE user_id = #{userId}")
    Long countLikes(@Param("userId") Long userId);

    /**
     * 查询关注列表（包含用户详细信息）
     */
    List<UserFollowListItemDTO> getFollowingListWithUser(@Param("userId") Long userId, @Param("currentUserId") Long currentUserId);

    /**
     * 查询粉丝列表（包含用户详细信息）
     */
    List<UserFollowListItemDTO> getFollowersListWithUser(@Param("userId") Long userId, @Param("currentUserId") Long currentUserId);
}
