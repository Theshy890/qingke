package com.qingke.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 绿植识别实体类
 */
@Data
@TableName("plant_recognize")
public class PlantRecognize implements Serializable {
    
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
     * 图片
     */
    private String imgUrl;
    
    /**
     * 链接
     */
    private String detailLink;
    
    /**
     * 植物介绍
     */
    private String plantIntro;
    
    /**
     * 病害名称
     */
    private String diseaseName;
    
    /**
     * 病害描述
     */
    private String diseaseDesc;
    
    /**
     * 治疗建议
     */
    private String treatSuggest;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}