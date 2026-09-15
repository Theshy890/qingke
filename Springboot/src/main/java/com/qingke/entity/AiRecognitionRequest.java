package com.qingke.entity;

import lombok.Data;

/**
 * AI识别请求类
 */
@Data
public class AiRecognitionRequest {
    
    /**
     * 图片URL或Base64
     */
    private String image;
    
    /**
     * 用户ID
     */
    private Long userid;
    
    /**
     * 识别类型：plant-植物识别，disease-病害识别
     */
    private String type;
     
}
