package com.qingke.service;

import com.qingke.entity.AiRecognitionResponse;

public interface ZhipuAiService {
    
    AiRecognitionResponse recognizePlantDisease(String imageBase64);

    /**
     * AI 客服对话（带上下文记忆）
     * @param question 用户问题
     * @param recentHistory 最近聊天记录（作为上下文）
     * @return AI 回复内容
     */
    String chat(String question, java.util.List<com.qingke.entity.PlantChat> recentHistory);

    /**
     * AI 客服多模态对话（文字+图片，带上下文记忆）
     * @param question 用户问题
     * @param imageBase64 图片base64数据
     * @param recentHistory 最近聊天记录（作为上下文）
     * @return AI 回复内容
     */
    String chatWithImage(String question, String imageBase64, java.util.List<com.qingke.entity.PlantChat> recentHistory);
}