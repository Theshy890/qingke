package com.qingke.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("community_comment")
public class CommunityComment implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long postId;
    private Long userId;
    private String avatarUrl;
    private String nickname;
    private String content;
    private String reply;
    private Integer likeCount;
    private Integer dislikeCount;
    private Integer isTop;
    private String likeUserIds;
    private String dislikeUserIds;
    private Long parentId;        // 父评论ID，NULL表示主评论
    private Long replyToUserId;  // 回复的目标用户ID

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
