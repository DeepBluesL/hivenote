package com.hivenote.controller;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hivenote.common.Result;
import com.hivenote.entity.Note;
import com.hivenote.entity.NoteLike;
import com.hivenote.entity.User;
import com.hivenote.service.INoteLikeService;
import com.hivenote.service.INoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/like")
public class NoteLikeController {

    @Autowired
    private INoteLikeService noteLikeService;

    @Autowired
    private INoteService noteService;

    /**
     * 切换点赞状态
     */
    @PostMapping("/toggle")
    public Result<Map<String, Object>> toggle(@RequestBody Map<String, String> params,
                                               @RequestHeader("accessToken") String token) {
        User currentUser = UserController.sessionMap.get(token);
        if (currentUser == null) {
            return Result.error(401, "请先登录");
        }

        String noteId = params.get("noteId");
        if (noteId == null || noteId.trim().isEmpty()) {
            return Result.error("笔记ID不能为空");
        }

        QueryWrapper<NoteLike> qw = new QueryWrapper<>();
        qw.eq("note_id", noteId);
        qw.eq("username", currentUser.getUsername());
        NoteLike existing = noteLikeService.getOne(qw);

        Map<String, Object> result = new HashMap<>();

        if (existing != null) {
            noteLikeService.removeById(existing.getId());
            updateLikeCount(noteId, -1);
            result.put("liked", false);
            result.put("message", "已取消点赞");
        } else {
            NoteLike like = new NoteLike();
            like.setId(IdUtil.getSnowflakeNextIdStr());
            like.setNoteId(noteId);
            like.setUsername(currentUser.getUsername());
            like.setCreateTime(new Date());
            noteLikeService.save(like);
            updateLikeCount(noteId, 1);
            result.put("liked", true);
            result.put("message", "点赞成功");
        }

        return Result.success(result);
    }

    /**
     * 检查是否已点赞
     */
    @GetMapping("/check")
    public Result<Map<String, Object>> check(@RequestParam String noteId,
                                              @RequestHeader("accessToken") String token) {
        User currentUser = UserController.sessionMap.get(token);
        if (currentUser == null) {
            return Result.error(401, "请先登录");
        }

        QueryWrapper<NoteLike> qw = new QueryWrapper<>();
        qw.eq("note_id", noteId);
        qw.eq("username", currentUser.getUsername());
        long count = noteLikeService.count(qw);

        Map<String, Object> result = new HashMap<>();
        result.put("liked", count > 0);
        return Result.success(result);
    }

    private void updateLikeCount(String noteId, int delta) {
        Note note = noteService.getById(noteId);
        if (note != null) {
            int current = note.getLikeCount() != null ? note.getLikeCount() : 0;
            note.setLikeCount(Math.max(0, current + delta));
            noteService.updateById(note);
        }
    }
}
