package com.qingke.entity;

import lombok.Data;

/**
 * AI识别响应类
 */
@Data
public class AiRecognitionResponse {
    
    /**
     * 植物名称
     */
    private String plantName;
    
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
    private String treatment;
    
    /**
     * 置信度
     */
    private Double confidence;
    
    /**
     * 是否检测到病害
     */
    private Boolean hasDisease;
}
