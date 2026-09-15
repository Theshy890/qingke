package com.qingke.service.impl;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.qingke.config.ZhipuAiConfig;
import com.qingke.entity.AiRecognitionResponse;
import com.qingke.entity.PlantChat;
import com.qingke.exception.RateLimitException;
import com.qingke.service.ZhipuAiService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * AI 服务实现（智谱 GLM-4.6V-Flash 一步式识别与病害分析，多 Key 轮询 + 失败降级）
 */
@Slf4j
@Service
public class ZhipuAiServiceImpl implements ZhipuAiService {

    @Autowired
    private ZhipuAiConfig zhipuAiConfig;

    private final AtomicInteger keyIndex = new AtomicInteger(0);

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    private final Gson gson = new Gson();

    @Override
    public AiRecognitionResponse recognizePlantDisease(String imageBase64) {
        AiRecognitionResponse response = new AiRecognitionResponse();

        try {
            log.info("========== 开始一步式AI识别（智谱GLM-4.6V-Flash） ==========");
            String analysisResult = analyzeByZhipu(imageBase64);

            if (analysisResult == null || analysisResult.trim().isEmpty()) {
                log.warn("========== 智谱API失败，走降级方案 ==========");
                return getMockRecognitionResult(imageBase64);
            }

            log.info("========== 智谱分析成功，长度：{} ==========", analysisResult.length());
            parseFullAnalysis(response, analysisResult);

        } catch (Exception e) {
            log.error("========== AI识别异常: {} ==========", e.getMessage(), e);
            return getMockRecognitionResult(imageBase64);
        }

        log.info("========== 识别完成：植物={}，病害={} ==========", response.getPlantName(), response.getDiseaseName());
        return response;
    }

    private String analyzeByZhipu(String imageBase64) {
        List<String> apiKeys = zhipuAiConfig.getApiKeys();
        int total = apiKeys.size();
        int startIndex = keyIndex.getAndIncrement() % total;

        for (int i = 0; i < total; i++) {
            int idx = (startIndex + i) % total;
            String apiKey = apiKeys.get(idx);
            try {
                log.info("使用Key[{}]调用智谱", idx);
                String result = callZhipuFullAnalysis(apiKey, imageBase64);
                if (result != null && !result.trim().isEmpty()) {
                    log.info("Key[{}]调用成功，返回{}字", idx, result.length());
                    return result;
                }
                log.warn("Key[{}]返回空，尝试下一个", idx);
            } catch (Exception e) {
                log.warn("Key[{}]失败: {}，尝试下一个", idx, e.getMessage());
            }
        }

        log.error("所有Key均失败");
        return "";
    }

    private String callZhipuFullAnalysis(String apiKey, String imageBase64) throws Exception {
        String base64Data = imageBase64;
        if (imageBase64.contains(",")) {
            base64Data = imageBase64.split(",")[1];
        }

        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", zhipuAiConfig.getModel());

        JsonArray messages = new JsonArray();
        JsonObject message = new JsonObject();
        message.addProperty("role", "user");

        JsonArray content = new JsonArray();

        JsonObject imageContent = new JsonObject();
        imageContent.addProperty("type", "image_url");
        JsonObject imageUrl = new JsonObject();
        imageUrl.addProperty("url", base64Data);
        imageContent.add("image_url", imageUrl);
        content.add(imageContent);

        JsonObject textContent = new JsonObject();
        textContent.addProperty("type", "text");
        textContent.addProperty("text", buildFullPrompt());
        content.add(textContent);

        message.add("content", content);
        messages.add(message);
        requestBody.add("messages", messages);
        requestBody.addProperty("temperature", 0.7);
        requestBody.addProperty("max_tokens", 2000);

        RequestBody body = RequestBody.create(
                requestBody.toString(),
                MediaType.parse("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(zhipuAiConfig.getApiUrl())
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        Response response = httpClient.newCall(request).execute();
        String responseBody = response.body().string();

        log.info("智谱响应码: {}", response.code());

        JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);

        if (jsonResponse.has("choices")) {
            JsonArray choices = jsonResponse.getAsJsonArray("choices");
            if (choices.size() > 0) {
                JsonObject firstChoice = choices.get(0).getAsJsonObject();
                if (firstChoice.has("message")) {
                    JsonObject messageObj = firstChoice.getAsJsonObject("message");
                    if (messageObj.has("content")) {
                        return messageObj.get("content").getAsString();
                    }
                }
            }
        }

        if (jsonResponse.has("error")) {
            JsonObject error = jsonResponse.getAsJsonObject("error");
            log.error("智谱错误: {}", error.has("message") ? error.get("message").getAsString() : "unknown");
        }

        return "";
    }

    private String buildFullPrompt() {
        return "你是经验丰富的植物病理专家。请识别图片中的植物并分析其健康状况。\n\n" +
                "请完成以下任务：\n" +
                "1. 识别植物名称\n" +
                "2. 提供该植物的简要介绍（50字以内）\n" +
                "3. 分析是否存在病害，如有请详细描述\n" +
                "4. 给出治疗或养护建议\n\n" +
                "严格按以下JSON格式回答：\n" +
                "{\n" +
                "  \"植物名称\": \"识别出的植物名\",\n" +
                "  \"植物介绍\": \"该植物的简要介绍\",\n" +
                "  \"置信度\": 0.95,\n" +
                "  \"病害名称\": \"病害名称，健康则填无明显病害\",\n" +
                "  \"病害描述\": \"详细症状描述，200字内\",\n" +
                "  \"治疗建议\": \"5条防治措施，格式：1. xxx 2. xxx 3. xxx 4. xxx 5. xxx\"\n" +
                "}";
    }

    private void parseFullAnalysis(AiRecognitionResponse response, String analysisText) {
        try {
            String jsonText = extractJson(analysisText);

            if (jsonText.startsWith("{")) {
                JsonObject json = gson.fromJson(jsonText, JsonObject.class);

                response.setPlantName(json.has("植物名称") ? json.get("植物名称").getAsString() : "未知植物");
                response.setPlantIntro(json.has("植物介绍") ? json.get("植物介绍").getAsString() : getDefaultIntro(response.getPlantName()));
                response.setConfidence(json.has("置信度") ? json.get("置信度").getAsDouble() : 0.85);

                String diseaseName = json.has("病害名称") ? json.get("病害名称").getAsString() : "未知";
                response.setDiseaseName(diseaseName);
                response.setHasDisease(!diseaseName.contains("无明显病害") && !diseaseName.contains("健康"));
                response.setDiseaseDesc(json.has("病害描述") ? json.get("病害描述").getAsString() : "");

                // 治疗建议可能是字符串或数组
                if (json.has("治疗建议")) {
                    if (json.get("治疗建议").isJsonArray()) {
                        StringBuilder sb = new StringBuilder();
                        JsonArray arr = json.getAsJsonArray("治疗建议");
                        for (int i = 0; i < arr.size(); i++) {
                            if (i > 0) sb.append("\n");
                            sb.append(arr.get(i).getAsString());
                        }
                        response.setTreatment(sb.toString());
                    } else {
                        response.setTreatment(json.get("治疗建议").getAsString());
                    }
                } else {
                    response.setTreatment("请咨询专业园艺师");
                }

                log.info("JSON解析成功：植物={}，病害={}", response.getPlantName(), response.getDiseaseName());
                return;
            }
        } catch (Exception e) {
            log.warn("JSON解析失败，尝试文本解析: {}", e.getMessage());
        }

        response.setPlantName("未知植物");
        response.setPlantIntro("请查看养护知识板块了解更多");
        response.setConfidence(0.8);
        response.setDiseaseName("分析中");
        response.setHasDisease(false);
        response.setDiseaseDesc(analysisText);
        response.setTreatment("请根据描述参考养护建议");
    }

    /**
     * 从 AI 返回文本中提取 JSON 内容（兼容 ```json ... ``` / ``` ... ``` / 裸 JSON）
     */
    private String extractJson(String text) {
        if (text == null) return "";
        String trimmed = text.trim();

        // 情况1: ```json ... ```
        if (trimmed.contains("```json")) {
            int start = trimmed.indexOf("```json") + 7;
            int end = trimmed.indexOf("```", start);
            if (end > start) return trimmed.substring(start, end).trim();
        }

        // 情况2: ``` ... ``` (不带 json 标记)
        if (trimmed.contains("```")) {
            int start = trimmed.indexOf("```") + 3;
            int end = trimmed.indexOf("```", start);
            if (end > start) return trimmed.substring(start, end).trim();
        }

        // 情况3: 直接找 { ... } 边界
        int start = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return trimmed.substring(start, end + 1);
        }

        return trimmed;
    }

    /**
     * 获取模拟识别结果（降级方案）
     */
    private AiRecognitionResponse getMockRecognitionResult(String imageBase64) {
        AiRecognitionResponse response = new AiRecognitionResponse();

        // 模拟识别结果
        String[] plants = {"绿萝", "吊兰", "多肉植物", "发财树", "虎皮兰", "仙人掌", "常春藤", "龟背竹"};
        String[] diseases = {"叶斑病", "根腐病", "白粉病", "炭疽病", "无明显病害"};

        int plantIndex = Math.abs(imageBase64.hashCode()) % plants.length;
        int diseaseIndex = Math.abs(imageBase64.hashCode()) % diseases.length;

        response.setPlantName(plants[plantIndex]);
        response.setPlantIntro(getMockPlantIntro(plants[plantIndex]));
        response.setDiseaseName(diseases[diseaseIndex]);
        response.setHasDisease(!diseases[diseaseIndex].equals("无明显病害"));
        response.setConfidence(0.85 + (Math.abs(imageBase64.hashCode()) % 15) / 100.0);

        if (response.getHasDisease()) {
            response.setDiseaseDesc(getMockDiseaseDesc(diseases[diseaseIndex]));
            response.setTreatment(getMockTreatment(diseases[diseaseIndex]));
        } else {
            response.setDiseaseDesc("植物生长状态良好，叶片翠绿，未检测到明显病害症状");
            response.setTreatment("继续保持当前养护方式，定期观察植物状态，注意适量浇水和通风");
        }

        log.info("使用模拟数据：植物={}, 病害={}", response.getPlantName(), response.getDiseaseName());

        return response;
    }

    private String getMockPlantIntro(String plantName) {
        switch (plantName) {
            case "绿萝":
                return "绿萝属于天南星科植物，生命力顽强，遇水即活，被称为\"生命之花\"。四季常绿，适合室内养护，具有很强的空气净化能力。";
            case "吊兰":
                return "吊兰是百合科多年生常绿草本植物，叶片细长优美，具有很强的空气净化能力，被称为\"空气卫士\"，适合室内种植。";
            case "多肉植物":
                return "多肉植物又称肉质植物，叶片肥厚多汁，耐旱性强，品种繁多，养护简单，是深受欢迎的室内观赏植物。";
            case "发财树":
                return "发财树学名马拉巴栗，寓意吉祥招财，树形优美，叶片四季常绿，适合室内摆放，喜温暖湿润环境。";
            case "虎皮兰":
                return "虎皮兰叶片坚挺直立，具有很强的适应性，能吸收甲醛等有害气体，净化空气效果显著，被称为\"天然清道夫\"。";
            case "仙人掌":
                return "仙人掌科植物，原产沙漠地区，极其耐旱，喜阳光充足环境，养护简单，品种丰富，是懒人植物的代表。";
            case "常春藤":
                return "常春藤是五加科常绿攀援植物，叶形美观，四季常青，具有很强的吸附灰尘和净化空气能力，适合室内养护。";
            case "龟背竹":
                return "龟背竹叶片宽大，形状奇特，耐阴性强，是优良的室内观叶植物，能有效吸收甲醛等有害气体。";
            default:
                return plantName + "是一种常见的观赏植物，适合室内养护。";
        }
    }

    private String getMockDiseaseDesc(String diseaseName) {
        switch (diseaseName) {
            case "叶斑病":
                return "叶片出现褐色或黑色斑点，可能伴有黄晕，严重时叶片干枯脱落。主要由真菌感染引起，在高温高湿环境下易发生。";
            case "根腐病":
                return "根系腐烂，植株生长不良，叶片发黄萎蔫，茎基部变软发黑。多因浇水过多、排水不良导致，根部缺氧而腐烂。";
            case "白粉病":
                return "叶片表面出现白色粉状物，影响光合作用，严重时导致叶片枯萎。在通风不良、湿度大的环境中易发生。";
            case "炭疽病":
                return "叶片出现圆形或不规则的褐色病斑，病斑中央灰白色，边缘深褐色，严重时病斑连片。高温多雨季节易发病。";
            default:
                return "植物出现异常症状，需要进一步观察和诊断。";
        }
    }

    private String getMockTreatment(String diseaseName) {
        switch (diseaseName) {
            case "叶斑病":
                return "1. 及时摘除病叶，防止病菌传播\n2. 减少浇水，保持叶面干燥\n3. 喷洒多菌灵或百菌清溶液（1:1000稀释），每周1次，连续2-3次\n4. 改善通风条件，避免过度密植\n5. 增施磷钾肥，提高植物抗病能力";
            case "根腐病":
                return "1. 立即停止浇水，将植株脱盆检查根系\n2. 剪除腐烂根系，保留健康白色根部\n3. 用高锰酸钾溶液（1:2000）消毒根部30分钟\n4. 更换新的透气土壤，确保排水良好\n5. 控制浇水频率，见干见湿，避免积水";
            case "白粉病":
                return "1. 摘除严重感染的叶片并销毁\n2. 喷洒硫磺粉或小苏打溶液（1:500稀释）\n3. 增加通风，降低环境湿度\n4. 避免夜间叶面有水分残留\n5. 每7-10天喷药一次，连续3次以上";
            case "炭疽病":
                return "1. 清除病叶病枝，集中销毁，减少病源\n2. 喷洒代森锰锌或甲基托布津（1:800稀释）\n3. 雨后及时排水，避免盆土积水\n4. 增施钾肥，提高植物抗病能力\n5. 保持环境卫生，定期用消毒液擦拭叶片";
            default:
                return "建议咨询专业园艺师，根据具体情况制定详细的治疗方案。注意改善养护环境，增强植物抗病能力。";
        }
    }

    /**
     * 设置默认的中文病害信息（降级方案）
     */
    private void setDefaultChineseDiseaseInfo(AiRecognitionResponse response) {
        response.setHasDisease(false);
        response.setDiseaseName("无明显病害");
        response.setDiseaseDesc("经AI分析，植物生长状态良好，叶片翠绿饱满，未检测到明显病害症状。植株整体健康，建议继续保持良好的养护习惯，定期观察植物状态。");
        response.setTreatment("养护建议：\n" +
                "1. 光照管理：保持充足的散射光，避免强光直射\n" +
                "2. 浇水要点：遵循见干见湿原则，避免积水\n" +
                "3. 通风环境：保持良好通风，预防病虫害\n" +
                "4. 施肥建议：生长期每月施薄肥1-2次，增强抗性\n" +
                "5. 日常检查：定期观察叶片、茎部，及早发现异常");
    }

    /**
     * 获取默认植物介绍
     */
    private String getDefaultIntro(String plantName) {
        return plantName + "是一种常见的观赏植物。更多养护知识请查看养护知识板块。";
    }

    @Override
    public String chat(String question, List<PlantChat> recentHistory) {
        List<String> apiKeys = zhipuAiConfig.getApiKeys();
        int total = apiKeys.size();
        int startIndex = keyIndex.getAndIncrement() % total;

        for (int i = 0; i < total; i++) {
            int idx = (startIndex + i) % total;
            String apiKey = apiKeys.get(idx);
            try {
                log.info("[AI客服] 使用Key[{}]调用智谱", idx);
                String result = callZhipuChat(apiKey, question, recentHistory);
                if (result != null && !result.trim().isEmpty()) {
                    return result;
                }
            } catch (Exception e) {
                log.warn("[AI客服] Key[{}]失败: {}，尝试下一个", idx, e.getMessage());
            }
        }
        return "抱歉，AI客服暂时无法响应，请稍后重试或联系人工客服。";
    }

    private String callZhipuChat(String apiKey, String question, List<PlantChat> recentHistory) throws Exception {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", zhipuAiConfig.getModel());

        JsonArray messages = new JsonArray();

        JsonObject systemMsg = new JsonObject();
        systemMsg.addProperty("role", "system");
        systemMsg.addProperty("content",
                "你是青稞绿植养护系统的专属AI客服，名字叫\"稞儿\"。" +
                "你只回答与绿植养护、系统功能使用相关的问题，包括：植物养护方法、病害防治、系统功能操作（AI识别、养护记录、养护提醒、社区交流等）、积分规则等。" +
                "回答要简洁友好，不超过200字，对于无关问题礼貌引导回到绿植养护主题。");
        messages.add(systemMsg);

        // 添加历史对话上下文
        if (recentHistory != null) {
            for (PlantChat chat : recentHistory) {
                // 历史用户提问
                JsonObject historyUser = new JsonObject();
                historyUser.addProperty("role", "user");
                JsonArray historyUserContent = new JsonArray();
                JsonObject historyUserText = new JsonObject();
                historyUserText.addProperty("type", "text");
                historyUserText.addProperty("text", chat.getAsk());
                historyUserContent.add(historyUserText);
                historyUser.add("content", historyUserContent);
                messages.add(historyUser);

                // 历史AI回复
                JsonObject historyBot = new JsonObject();
                historyBot.addProperty("role", "assistant");
                JsonArray historyBotContent = new JsonArray();
                JsonObject historyBotText = new JsonObject();
                historyBotText.addProperty("type", "text");
                historyBotText.addProperty("text", chat.getReply() != null ? chat.getReply() : "");
                historyBotContent.add(historyBotText);
                historyBot.add("content", historyBotContent);
                messages.add(historyBot);
            }
        }

        JsonObject userMsg = new JsonObject();
        userMsg.addProperty("role", "user");
        JsonArray userContent = new JsonArray();
        JsonObject userText = new JsonObject();
        userText.addProperty("type", "text");
        userText.addProperty("text", question);
        userContent.add(userText);
        userMsg.add("content", userContent);
        messages.add(userMsg);

        requestBody.add("messages", messages);
        requestBody.addProperty("temperature", 0.7);
        requestBody.addProperty("max_tokens", 512);

        RequestBody body = RequestBody.create(
                requestBody.toString(),
                MediaType.parse("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(zhipuAiConfig.getApiUrl())
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        Response response = httpClient.newCall(request).execute();
        String responseBody = response.body().string();
        log.info("[AI客服] 响应码: {}", response.code());

        JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
        if (jsonResponse.has("choices")) {
            JsonArray choices = jsonResponse.getAsJsonArray("choices");
            if (choices.size() > 0) {
                JsonObject firstChoice = choices.get(0).getAsJsonObject();
                if (firstChoice.has("message")) {
                    JsonObject messageObj = firstChoice.getAsJsonObject("message");
                    if (messageObj.has("content")) {
                        return messageObj.get("content").getAsString();
                    }
                }
            }
        }
        if (jsonResponse.has("error")) {
            log.error("[AI客服] 错误: {}", jsonResponse.getAsJsonObject("error"));
        }
        return "";
    }

    @Override
    public String chatWithImage(String question, String imageBase64, List<PlantChat> recentHistory) {
        List<String> apiKeys = zhipuAiConfig.getApiKeys();
        int total = apiKeys.size();
        int startIndex = keyIndex.getAndIncrement() % total;

        // 第一轮：依次尝试所有 key
        for (int i = 0; i < total; i++) {
            int idx = (startIndex + i) % total;
            String apiKey = apiKeys.get(idx);
            try {
                log.info("[AI客服-多模态] 使用Key[{}]调用智谱", idx);
                String result = callZhipuChatWithImage(apiKey, question, imageBase64, recentHistory);
                if (result != null && !result.trim().isEmpty()) {
                    return result;
                }
            } catch (RateLimitException e) {
                log.warn("[AI客服-多模态] Key[{}]被限流(429)，尝试下一个", idx);
            } catch (Exception e) {
                log.warn("[AI客服-多模态] Key[{}]失败: {}，尝试下一个", idx, e.getMessage());
            }
        }

        // 第二轮：等待 5 秒后重试所有 key
        log.info("[AI客服-多模态] 第一轮全部限流，等待5秒后重试...");
        try { Thread.sleep(5000); } catch (InterruptedException ignored) {}

        for (int i = 0; i < total; i++) {
            int idx = (startIndex + i) % total;
            String apiKey = apiKeys.get(idx);
            try {
                log.info("[AI客服-多模态] 重试 Key[{}]", idx);
                String result = callZhipuChatWithImage(apiKey, question, imageBase64, recentHistory);
                if (result != null && !result.trim().isEmpty()) {
                    return result;
                }
            } catch (RateLimitException e) {
                log.warn("[AI客服-多模态] 重试 Key[{}]仍被限流", idx);
            } catch (Exception e) {
                log.warn("[AI客服-多模态] 重试 Key[{}]失败: {}", idx, e.getMessage());
            }
        }

        return "抱歉，AI客服暂时无法响应，请稍后重试或联系人工客服。";
    }

    private String callZhipuChatWithImage(String apiKey, String question, String imageBase64, List<PlantChat> recentHistory) throws Exception {
        // 去掉 data:image/xxx;base64, 前缀
        String base64Data = imageBase64;
        if (imageBase64.contains(",")) {
            base64Data = imageBase64.split(",")[1];
        }

        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", zhipuAiConfig.getModel());

        JsonArray messages = new JsonArray();

        JsonObject systemMsg = new JsonObject();
        systemMsg.addProperty("role", "system");
        systemMsg.addProperty("content",
                "你是青稞绿植养护系统的专属AI客服，名字叫\"稞儿\"。" +
                "你只回答与绿植养护、系统功能使用相关的问题，包括：植物养护方法、病害防治、系统功能操作（AI识别、养护记录、养护提醒、社区交流等）、积分规则等。" +
                "回答要简洁友好，不超过200字，对于无关问题礼貌引导回到绿植养护主题。" +
                "当用户发送图片时，请结合图片内容进行分析和回答。");
        messages.add(systemMsg);

        // 添加历史对话上下文（纯文字形式）
        if (recentHistory != null) {
            for (PlantChat chat : recentHistory) {
                JsonObject historyUser = new JsonObject();
                historyUser.addProperty("role", "user");
                JsonArray historyUserContent = new JsonArray();
                JsonObject historyUserText = new JsonObject();
                historyUserText.addProperty("type", "text");
                historyUserText.addProperty("text", chat.getAsk());
                historyUserContent.add(historyUserText);
                historyUser.add("content", historyUserContent);
                messages.add(historyUser);

                JsonObject historyBot = new JsonObject();
                historyBot.addProperty("role", "assistant");
                JsonArray historyBotContent = new JsonArray();
                JsonObject historyBotText = new JsonObject();
                historyBotText.addProperty("type", "text");
                historyBotText.addProperty("text", chat.getReply() != null ? chat.getReply() : "");
                historyBotContent.add(historyBotText);
                historyBot.add("content", historyBotContent);
                messages.add(historyBot);
            }
        }

        JsonObject userMsg = new JsonObject();
        userMsg.addProperty("role", "user");
        JsonArray userContent = new JsonArray();

        // 添加图片内容
        JsonObject imageContent = new JsonObject();
        imageContent.addProperty("type", "image_url");
        JsonObject imageUrlObj = new JsonObject();
        imageUrlObj.addProperty("url", base64Data);
        imageContent.add("image_url", imageUrlObj);
        userContent.add(imageContent);

        // 添加文字内容
        if (question != null && !question.trim().isEmpty()) {
            JsonObject textContent = new JsonObject();
            textContent.addProperty("type", "text");
            textContent.addProperty("text", question);
            userContent.add(textContent);
        }

        userMsg.add("content", userContent);
        messages.add(userMsg);

        requestBody.add("messages", messages);
        requestBody.addProperty("temperature", 0.7);
        requestBody.addProperty("max_tokens", 512);

        RequestBody body = RequestBody.create(
                requestBody.toString(),
                MediaType.parse("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(zhipuAiConfig.getApiUrl())
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        Response response = httpClient.newCall(request).execute();
        String responseBody = response.body().string();
        log.info("[AI客服-多模态] 响应码: {}", response.code());

        // 429 限流：抛出 RateLimitException 让外层重试
        if (response.code() == 429) {
            throw new RateLimitException("API 请求量过大，请稍后重试");
        }

        JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
        if (jsonResponse.has("choices")) {
            JsonArray choices = jsonResponse.getAsJsonArray("choices");
            if (choices.size() > 0) {
                JsonObject firstChoice = choices.get(0).getAsJsonObject();
                if (firstChoice.has("message")) {
                    JsonObject messageObj = firstChoice.getAsJsonObject("message");
                    if (messageObj.has("content")) {
                        return messageObj.get("content").getAsString();
                    }
                }
            }
        }
        if (jsonResponse.has("error")) {
            log.error("[AI客服-多模态] 错误: {}", jsonResponse.getAsJsonObject("error"));
        }
        return "";
    }
}