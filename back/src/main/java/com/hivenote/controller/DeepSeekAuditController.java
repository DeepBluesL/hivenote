package com.hivenote.controller;

import com.hivenote.common.Result;
import com.hivenote.entity.Note;
import com.hivenote.service.DeepSeekAuditService;
import com.hivenote.service.INoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/audit")
public class DeepSeekAuditController {

    @Autowired
    private DeepSeekAuditService auditService;
    
    @Autowired
    private INoteService noteService;
    
    /**
     * AI预审核单条笔记
     */
    @GetMapping("/ai/{id}")
    public Result<DeepSeekAuditService.AuditResult> aiAudit(@PathVariable String id) {
        Note note = noteService.getById(id);
        if (note == null) {
            return Result.error("笔记不存在");
        }
        
        DeepSeekAuditService.AuditResult result = auditService.auditNote(note);
        return Result.success(result);
    }
    
    /**
     * AI批量预审核
     */
    @PostMapping("/ai/batch")
    public Result<Map<String, DeepSeekAuditService.AuditResult>> aiBatchAudit(@RequestBody List<String> ids) {
        List<Note> notes = noteService.listByIds(ids);
        
        List<DeepSeekAuditService.AuditResult> results = auditService.batchAudit(notes);
        
        Map<String, DeepSeekAuditService.AuditResult> resultMap = new HashMap<>();
        for (DeepSeekAuditService.AuditResult result : results) {
            resultMap.put(result.getNoteId(), result);
        }
        
        return Result.success(resultMap);
    }
    
    /**
     * AI审核待处理列表
     */
    @GetMapping("/ai/pending")
    public Result<List<DeepSeekAuditService.AuditResult>> aiAuditPending(
            @RequestParam(defaultValue = "10") int limit) {
        
        // 获取待审核笔记（status=0）
        List<Note> pendingNotes = noteService.lambdaQuery()
                .eq(Note::getStatus, 0)
                .eq(Note::getDelFlag, 0)
                .orderByDesc(Note::getCreateTime)
                .last("LIMIT " + limit)
                .list();
        
        List<DeepSeekAuditService.AuditResult> results = auditService.batchAudit(pendingNotes);
        return Result.success(results);
    }
    
    /**
     * 根据AI建议自动审核通过
     */
    @PostMapping("/ai/auto-approve/{id}")
    public Result<Object> autoApprove(@PathVariable String id) {
        Note note = noteService.getById(id);
        if (note == null) {
            return Result.error("笔记不存在");
        }
        
        // AI审核
        DeepSeekAuditService.AuditResult aiResult = auditService.auditNote(note);
        
        // 如果AI判定为高风险，不给自动通过
        if ("HIGH".equals(aiResult.getRiskLevel()) || 
            "REJECT".equals(aiResult.getSuggestion())) {
            return Result.error("AI判定为高风险，需要人工审核");
        }
        
        // 如果AI判定为低风险且置信度高，自动通过
        if ("LOW".equals(aiResult.getRiskLevel()) && aiResult.getConfidence() > 0.9) {
            note.setStatus(1); // 通过
            noteService.updateById(note);
            
            Map<String, Object> result = new HashMap<>();
            result.put("message", "AI自动审核通过");
            result.put("aiResult", aiResult);
            return Result.success(result);
        }
        
        return Result.error("AI建议人工复审");
    }
}
