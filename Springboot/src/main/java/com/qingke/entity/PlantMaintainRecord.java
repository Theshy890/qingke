package com.qingke.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 养护记录实体类
 */
@Data
@TableName("plant_maintain_record")
public class PlantMaintainRecord implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 绿植名称
     */
    private String plantName;
    
    /**
     * 绿植种类
     */
    private String categoryName;
    
    /**
     * 绿植图片
     */
    private String imgUrl;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 账号
     */
    private String userAccount;
    
    /**
     * 生长状态
     */
    private String growStatus;
    
    /**
     * 养护记录
     */
    private String maintainContent;
    
    /**
     * 养护日期
     */
    private String maintainDate;
    
    /**
     * 养护周期（如：每7天、每月）
     */
    private String maintainCycle;
    
    /**
     * 下次养护时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date nextMaintainTime;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}