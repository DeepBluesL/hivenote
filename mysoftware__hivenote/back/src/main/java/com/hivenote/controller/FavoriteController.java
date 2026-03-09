package com.hivenote.controller;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hivenote.common.Result;
import com.hivenote.entity.Favorite;
import com.hivenote.entity.Note;
import com.hivenote.entity.User;
import com.hivenote.service.IFavoriteService;
import com.hivenote.service.INoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/favorite")
public class FavoriteController {

    @Autowired
    private IFavoriteService favoriteService;

    @Autowired
    private INoteService noteService;

    /**
     * 切换收藏状态（收藏/取消收藏）
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

        QueryWrapper<Favorite> qw = new QueryWrapper<>();
        qw.eq("note_id", noteId);
        qw.eq("username", currentUser.getUsername());
        Favorite existing = favoriteService.getOne(qw);

        Map<String, Object> result = new HashMap<>();

        if (existing != null) {
            favoriteService.removeById(existing.getId());
            updateFavoriteCount(noteId, -1);
            result.put("favorited", false);
            result.put("message", "已取消收藏");
        } else {
            Favorite favorite = new Favorite();
            favorite.setId(IdUtil.getSnowflakeNextIdStr());
            favorite.setNoteId(noteId);
            favorite.setUsername(currentUser.getUsername());
            favorite.setCreateTime(new Date());
            favoriteService.save(favorite);
            updateFavoriteCount(noteId, 1);
            result.put("favorited", true);
            result.put("message", "收藏成功");
        }

        return Result.success(result);
    }

    /**
     * 检查是否已收藏
     */
    @GetMapping("/check")
    public Result<Map<String, Object>> check(@RequestParam String noteId,
                                              @RequestHeader("accessToken") String token) {
        User currentUser = UserController.sessionMap.get(token);
        if (currentUser == null) {
            return Result.error(401, "请先登录");
        }

        QueryWrapper<Favorite> qw = new QueryWrapper<>();
        qw.eq("note_id", noteId);
        qw.eq("username", currentUser.getUsername());
        long count = favoriteService.count(qw);

        Map<String, Object> result = new HashMap<>();
        result.put("favorited", count > 0);
        return Result.success(result);
    }

    /**
     * 获取我的收藏列表
     */
    @GetMapping("/myList")
    public Result<List<Note>> getMyFavorites(@RequestHeader("accessToken") String token) {
        User currentUser = UserController.sessionMap.get(token);
        if (currentUser == null) {
            return Result.error(401, "请先登录");
        }

        QueryWrapper<Favorite> qw = new QueryWrapper<>();
        qw.eq("username", currentUser.getUsername());
        qw.orderByDesc("create_time");
        List<Favorite> favorites = favoriteService.list(qw);

        if (favorites.isEmpty()) {
            return Result.success(new ArrayList<>());
        }

        List<String> noteIds = favorites.stream()
                .map(Favorite::getNoteId)
                .collect(Collectors.toList());

        QueryWrapper<Note> noteQw = new QueryWrapper<>();
        noteQw.in("id", noteIds);
        noteQw.eq("del_flag", 0);
        List<Note> notes = noteService.list(noteQw);

        return Result.success(notes);
    }

    private void updateFavoriteCount(String noteId, int delta) {
        Note note = noteService.getById(noteId);
        if (note != null) {
            int current = note.getFavoriteCount() != null ? note.getFavoriteCount() : 0;
            note.setFavoriteCount(Math.max(0, current + delta));
            noteService.updateById(note);
        }
    }
}
