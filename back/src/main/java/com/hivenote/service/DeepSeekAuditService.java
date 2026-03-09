package com.hivenote.service;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.hivenote.entity.Note;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class DeepSeekAuditService {

    @Value("${deepseek.api.key:sk-您的API密钥}")
    private String apiKey;
    
    @Value("${deepseek.api.url:https://api.deepseek.com/v1/chat/completions}")
    private String apiUrl;
    
    /**
     * AI审核笔记
     */
    public AuditResult auditNote(Note note) {
        try {
            // 1. 构建审核提示词
            String prompt = buildAuditPrompt(note);
            
            // 2. 调用DeepSeek API
            String aiResponse = callDeepSeekAPI(prompt);
            
            // 3. 解析结果
            AuditResult result = parseAIResponse(aiResponse, note);
            
            // 4. 记录日志
            log.info("AI审核完成 - 笔记ID: {}, 风险等级: {}, 置信度: {}", 
                note.getId(), result.getRiskLevel(), result.getConfidence());
            
            return result;
            
        } catch (Exception e) {
            log.error("AI审核失败", e);
            return createErrorResult("AI审核服务调用失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量AI审核
     */
    public List<AuditResult> batchAudit(List<Note> notes) {
        List<AuditResult> results = new ArrayList<>();
        for (Note note : notes) {
            results.add(auditNote(note));
        }
        return results;
    }
    
    /**
     * 构建审核提示词
     */
    private String buildAuditPrompt(Note note) {
        return String.format("""
            你是一个专业的笔记内容审核员，请审核以下笔记内容，并给出审核建议。
            
            笔记信息：
            标题：%s
            简介：%s
            分类：%s
            上传者：%s
            学校：%s
            
            请从以下几个方面审核：
            1. 是否包含色情、低俗内容
            2. 是否包含政治敏感内容
            3. 是否包含广告、垃圾信息
            4. 是否包含暴力、恐怖内容
            5. 内容质量是否合格（是否完整、有意义）
            
            请按以下JSON格式返回结果，不要有其他说明文字：
            {
                "riskLevel": "HIGH/MEDIUM/LOW",
                "suggestion": "PASS/REVIEW/REJECT",
                "riskReason": "风险原因描述",
                "confidence": 0.95,
                "details": {
                    "pornScore": 0-100,
                    "politicalScore": 0-100,
                    "adScore": 0-100,
                    "violenceScore": 0-100,
                    "qualityScore": 0-100
                },
                "keywords": ["关键词1", "关键词2"]
            }
            
            注意：
            - riskLevel: HIGH=高风险，MEDIUM=中风险，LOW=低风险
            - suggestion: PASS=通过，REVIEW=人工复审，REJECT=拒绝
            - confidence: 置信度0-1之间
            - 分数越高表示问题越严重
            """, 
            note.getTitle(),
            note.getSummary() != null ? note.getSummary() : "无",
            note.getCategory(),
            note.getCreateBy(),
            note.getSchool() != null ? note.getSchool() : "未知"
        );
    }
    
    /**
     * 调用DeepSeek API
     */
    private String callDeepSeekAPI(String prompt) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "deepseek-chat");
        
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);
        messages.add(userMessage);
        
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.3);  // 低温度使输出更稳定
        requestBody.put("max_tokens", 1000);
        
        HttpResponse response = HttpRequest.post(apiUrl)
            .header("Authorization", "Bearer " + apiKey)
            .header("Content-Type", "application/json")
            .body(JSONUtil.toJsonStr(requestBody))
            .timeout(30000)  // 30秒超时
            .execute();
        
        if (response.getStatus() != 200) {
            throw new RuntimeException("API调用失败: " + response.body());
        }
        
        JSONObject jsonResponse = JSONUtil.parseObj(response.body());
        JSONArray choices = jsonResponse.getJSONArray("choices");
        if (choices == null || choices.isEmpty()) {
            throw new RuntimeException("API返回结果为空");
        }
        
        JSONObject firstChoice = choices.getJSONObject(0);
        JSONObject message = firstChoice.getJSONObject("message");
        return message.getStr("content");
    }
    
    /**
     * 解析AI响应
     */
    private AuditResult parseAIResponse(String aiResponse, Note note) {
        try {
            // 提取JSON部分（AI可能返回额外的说明文字）
            String jsonStr = extractJSON(aiResponse);
            JSONObject json = JSONUtil.parseObj(jsonStr);
            
            AuditResult result = new AuditResult();
            result.setNoteId(note.getId());
            result.setNoteTitle(note.getTitle());
            result.setRiskLevel(json.getStr("riskLevel"));
            result.setSuggestion(json.getStr("suggestion"));
            result.setRiskReason(json.getStr("riskReason"));
            result.setConfidence(json.getDouble("confidence"));
            
            // 解析详细分数
            JSONObject details = json.getJSONObject("details");
            if (details != null) {
                Map<String, Object> detailsMap = new HashMap<>();
                detailsMap.put("pornScore", details.getInt("pornScore"));
                detailsMap.put("politicalScore", details.getInt("politicalScore"));
                detailsMap.put("adScore", details.getInt("adScore"));
                detailsMap.put("violenceScore", details.getInt("violenceScore"));
                detailsMap.put("qualityScore", details.getInt("qualityScore"));
                result.setDetails(detailsMap);
            }
            
            // 解析关键词
            JSONArray keywords = json.getJSONArray("keywords");
            if (keywords != null) {
                List<String> keywordList = new ArrayList<>();
                for (int i = 0; i < keywords.size(); i++) {
                    keywordList.add(keywords.getStr(i));
                }
                result.setKeywords(keywordList);
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("解析AI响应失败: {}", aiResponse, e);
            return createErrorResult("AI响应解析失败");
        }
    }
    
    /**
     * 从文本中提取JSON
     */
    private String extractJSON(String text) {
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}') + 1;
        if (start >= 0 && end > start) {
            return text.substring(start, end);
        }
        return "{}";
    }
    
    /**
     * 创建错误结果
     */
    private AuditResult createErrorResult(String errorMsg) {
        AuditResult result = new AuditResult();
        result.setRiskLevel("ERROR");
        result.setSuggestion("REVIEW");
        result.setRiskReason(errorMsg);
        result.setConfidence(0.0);
        return result;
    }
    
    @Data
    public static class AuditResult {
        private String noteId;
        private String noteTitle;
        private String riskLevel;      // HIGH/MEDIUM/LOW/ERROR
        private String suggestion;      // PASS/REVIEW/REJECT
        private String riskReason;      // 风险原因
        private double confidence;      // 置信度 0-1
        private Map<String, Object> details; // 详细分析
        private List<String> keywords;  // 关键词
        private long auditTime = System.currentTimeMillis();
    }
}
