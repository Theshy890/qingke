package com.qingke.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 评论树DTO，用于返回主评论及其回复列表
 */
@Data
public class CommentTreeDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long postId;
    private Long userId;
    private String avatarUrl;
    private String nickname;
    private String content;
    private String reply;  // 管理员回复
    private Integer likeCount;
    private Integer isTop;
    private Long parentId;
    private Long replyToUserId;
    private String replyToUserName;  // 回复的目标用户名

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    // 子评论列表（回复列表）
    private List<CommentReplyDTO> replies;

    // 子评论总数（用于折叠显示）
    private Integer replyCount;
}
