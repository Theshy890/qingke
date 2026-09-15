package com.qingke.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 养护知识实体类
 */
@Data
@TableName("plant_knowledge")
public class PlantKnowledge implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 植物名称
     */
    private String plantName;
    
    /**
     * 绿植种类
     */
    private String categoryName;
    
    /**
     * 图片
     */
    private String imgUrl;
    
    /**
     * 视频
     */
    private String videoUrl;
    
    /**
     * 养护教程
     */
    private String maintainTutorial;
    
    /**
     * 发布时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date publishDate;
    
    /**
     * 最近点击时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lastClickTime;
    
    /**
     * 点击次数
     */
    private Integer clickNum;
    
    /**
     * 收藏数量
     */
    private Integer collectNum;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}