package com.qingke.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户主页信息传输对象
 */
@Data
public class UserProfileDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String avatarUrl;
    private String signature;
    private String gender;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date registerTime;

    private Integer points;

    // 隐私设置（是否可见）
    private Boolean careRecordsVisible;
    private Boolean favoritesVisible;

    // 统计数据
    private Long maintainRecordCount;
    private Long collectCount;
}
