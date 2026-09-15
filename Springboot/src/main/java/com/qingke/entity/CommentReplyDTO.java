package com.qingke.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 评论回复DTO，用于返回子评论（回复）信息
 */
@Data
public class CommentReplyDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long postId;
    private Long userId;
    private String avatarUrl;
    private String nickname;
    private String content;
    private Integer likeCount;
    private Long parentId;
    private Long replyToUserId;
    private String replyToUserName;  // 回复的目标用户名

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
