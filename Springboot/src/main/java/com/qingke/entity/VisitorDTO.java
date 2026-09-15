package com.qingke.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 访客记录详情 DTO
 */
@Data
public class VisitorDTO {
    private Long id;
    private Long visitorId; // 访客ID
    private String visitorNickname; // 访客昵称
    private String visitorAvatar; // 访客头像
    private Integer isRead; // 是否已读

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date visitTime; // 访问时间
}
