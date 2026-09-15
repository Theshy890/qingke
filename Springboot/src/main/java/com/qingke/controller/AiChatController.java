package com.qingke.controller;

import com.qingke.common.Result;
import com.qingke.entity.PlantChat;
import com.qingke.service.ZhipuAiService;
import com.qingke.service.PlantChatService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiChatController {

    @Autowired
    private ZhipuAiService zhipuAiService;

    @Autowired
    private PlantChatService plantChatService;

    /**
     * AI 客服对话（自动保存聊天记录到数据库）
     * 支持纯文字和多模态（文字+图片）两种模式
     * 附带最近 6 条历史记录作为上下文
     */
    @PostMapping("/chat")
    public Result chat(@RequestBody Map<String, String> body, HttpServletRequest request) {
        String question = body.get("question");
        String imageBase64 = body.get("imageBase64");

        // 纯文字模式：问题不能为空
        // 多模态模式：有图片时问题可以为空
        boolean hasImage = imageBase64 != null && !imageBase64.trim().isEmpty();
        if (!hasImage && (question == null || question.trim().isEmpty())) {
            return Result.error("问题不能为空");
        }

        Long userId = getCurrentUserId(request);

        // 获取最近 6 条聊天记录作为上下文（3 轮对话）
        List<PlantChat> recentHistory = null;
        if (userId != null) {
            List<PlantChat> allHistory = plantChatService.findByUserId(userId);
            if (allHistory != null && allHistory.size() > 6) {
                recentHistory = allHistory.subList(allHistory.size() - 6, allHistory.size());
            } else {
                recentHistory = allHistory;
            }
        }

        String answer;
        if (hasImage) {
            answer = zhipuAiService.chatWithImage(
                    question != null ? question.trim() : "请分析这张图片",
                    imageBase64.trim(),
                    recentHistory
            );
        } else {
            answer = zhipuAiService.chat(question.trim(), recentHistory);
        }

        // 保存到数据库
        if (userId != null) {
            PlantChat chat = new PlantChat();
            chat.setUserId(userId);
            chat.setAsk(question != null ? question.trim() : "[图片]");
            chat.setReply(answer);
            chat.setIsReplied(1);
            chat.setIsRead(1);
            chat.setMsgType("1");
            if (hasImage) {
                chat.setImageUrl(imageBase64.trim());
            }
            plantChatService.saveChat(chat);
        }

        return Result.success(answer);
    }

    /**
     * 获取当前用户的聊天记录
     */
    @GetMapping("/chat/history")
    public Result<List<PlantChat>> getHistory(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("请先登录");
        }
        List<PlantChat> list = plantChatService.findByUserId(userId);
        return Result.success(list);
    }

    /**
     * 删除当前用户的所有聊天记录
     */
    @DeleteMapping("/chat/history")
    public Result<?> deleteHistory(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        if (userId == null) {
            return Result.error("请先登录");
        }
        plantChatService.deleteByUserId(userId);
        return Result.success();
    }

    private Long getCurrentUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        if (token == null || token.isEmpty()) {
            return null;
        }
        try {
            return com.qingke.common.JwtUtil.getUserIdFromToken(token);
        } catch (Exception e) {
            return null;
        }
    }
}
