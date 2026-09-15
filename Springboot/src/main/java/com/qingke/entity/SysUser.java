package com.qingke.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("sys_user")
public class SysUser implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    private String zh;
    private String password;
    private String name;
    private String gender;
    private Integer age;
    private String phone;
    private String avatarUrl;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date registerTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lastLoginTime;

    private String email;
    private Integer status;
    private String role; // admin=超级管理员, user=APP普通用户
    private Integer passwordWrongNum;
    private Integer points;
    private Integer aiCount;
    private Integer newbieRewardReceived; // 新人专享是否已领取：0=未领取，1=已领取

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    private String profileVisibility; // 个人资料可见性: public-所有人, friends-仅好友, private-仅自己
    private Integer careRecordsPublic; // 养护记录是否公开: 0-否, 1-是
    private Integer favoritesPublic; // 收藏内容是否公开: 0-否, 1-是
    private String signature; // 个性签名

    @TableField(exist = false)
    private String oldPassword;

    @TableField(exist = false)
    private String newPassword;
}
