package com.hivenote.controller;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hivenote.common.Result;
import com.hivenote.entity.Comment;
import com.hivenote.entity.Note;
import com.hivenote.entity.Notification;
import com.hivenote.entity.User;
import com.hivenote.service.ICommentService;
import com.hivenote.service.INoteService;
import com.hivenote.service.INotificationService;
import com.hivenote.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/comment")
public class CommentController {

    @Autowired
    private ICommentService commentService;

    @Autowired
    private INoteService noteService;

    @Autowired
    private IUserService userService;

    @Autowired
    private INotificationService notificationService;

    /**
     * 获取某篇笔记的评论列表（支持层级结构）
     */
    @GetMapping("/getAllByPage")
    public Result<List<Map<String, Object>>> getList(@RequestParam String noteId) {
        QueryWrapper<Comment> qw = new QueryWrapper<>();
        qw.eq("note_id", noteId);
        qw.eq("del_flag", 0);
        qw.orderByAsc("create_time");

        List<Comment> allComments = commentService.list(qw);

        // 分离顶级评论和回复
        List<Comment> topLevel = allComments.stream()
                .filter(c -> c.getParentId() == null || c.getParentId().isEmpty())
                .collect(Collectors.toList());
        Map<String, List<Comment>> repliesMap = allComments.stream()
                .filter(c -> c.getParentId() != null && !c.getParentId().isEmpty())
                .collect(Collectors.groupingBy(Comment::getParentId));

        // 构建层级结构
        List<Map<String, Object>> result = new ArrayList<>();
        for (Comment comment : topLevel) {
            Map<String, Object> item = commentToMap(comment);
            List<Comment> replies = repliesMap.getOrDefault(comment.getId(), new ArrayList<>());
            item.put("replies", replies.stream().map(this::commentToMap).collect(Collectors.toList()));
            result.add(item);
        }

        // 按时间倒序排列顶级评论
        Collections.reverse(result);

        return Result.success(result);
    }

    private Map<String, Object> commentToMap(Comment c) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", c.getId());
        map.put("noteId", c.getNoteId());
        map.put("parentId", c.getParentId());
        map.put("replyTo", c.getReplyTo());
        map.put("content", c.getContent());
        map.put("username", c.getUsername());
        map.put("nickname", c.getNickname());
        map.put("userType", c.getUserType() != null ? c.getUserType() : 0);
        map.put("createTime", c.getCreateTime());
        return map;
    }

    /**
     * 发表评论 / 回复评论
     */
    @PostMapping("/save")
    public Result<Object> save(@RequestBody Comment comment,
                               @RequestHeader("accessToken") String token) {
        User currentUser = UserController.sessionMap.get(token);
        if (currentUser == null) {
            return Result.error(401, "请先登录");
        }

        if (comment.getNoteId() == null || comment.getNoteId().trim().isEmpty()) {
            return Result.error("笔记ID不能为空");
        }
        if (comment.getContent() == null || comment.getContent().trim().isEmpty()) {
            return Result.error("评论内容不能为空");
        }

        comment.setId(IdUtil.getSnowflakeNextIdStr());
        comment.setUsername(currentUser.getUsername());
        comment.setNickname(currentUser.getNickname());
        comment.setUserType(currentUser.getType() != null ? currentUser.getType() : 0);
        comment.setCreateTime(new Date());
        comment.setDelFlag(0);

        boolean saved = commentService.save(comment);

        if (saved) {
            sendCommentNotification(comment, currentUser);
            return Result.success("评论成功");
        } else {
            return Result.error("评论失败");
        }
    }

    /**
     * 获取我的评论
     */
    @GetMapping("/myList")
    public Result<List<Comment>> getMyComments(@RequestHeader("accessToken") String token) {
        User currentUser = UserController.sessionMap.get(token);
        if (currentUser == null) {
            return Result.error(401, "请先登录");
        }

        QueryWrapper<Comment> qw = new QueryWrapper<>();
        qw.eq("username", currentUser.getUsername());
        qw.eq("del_flag", 0);
        qw.orderByDesc("create_time");
        return Result.success(commentService.list(qw));
    }

    /**
     * 删除评论（本人或管理员可删除）
     */
    @DeleteMapping("/del/{id}")
    public Result<Object> delete(@PathVariable String id,
                                 @RequestHeader("accessToken") String token) {
        User currentUser = UserController.sessionMap.get(token);
        if (currentUser == null) {
            return Result.error(401, "请先登录");
        }

        Comment comment = commentService.getById(id);
        if (comment == null) {
            return Result.error("评论不存在");
        }

        boolean isAdmin = currentUser.getType() != null && currentUser.getType() == 1;
        boolean isOwner = currentUser.getUsername().equals(comment.getUsername());
        if (!isAdmin && !isOwner) {
            return Result.error("无权删除他人评论");
        }

        comment.setDelFlag(1);
        commentService.updateById(comment);

        QueryWrapper<Comment> qw = new QueryWrapper<>();
        qw.eq("parent_id", id);
        List<Comment> replies = commentService.list(qw);
        for (Comment reply : replies) {
            reply.setDelFlag(1);
            commentService.updateById(reply);
        }

        return Result.success("删除成功");
    }

    /**
     * 管理员获取所有评论列表（分页）
     */
    @GetMapping("/admin/list")
    public Result<Object> getAdminCommentList(@RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "20") int size,
                                             String keyword,
                                             @RequestHeader("accessToken") String token) {
        User currentUser = UserController.sessionMap.get(token);
        if (currentUser == null) {
            return Result.error(401, "请先登录");
        }
        if (currentUser.getType() == null || currentUser.getType() != 1) {
            return Result.error(403, "无权访问");
        }

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Comment> pageParam =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size);
        QueryWrapper<Comment> qw = new QueryWrapper<>();
        qw.eq("del_flag", 0);
        if (keyword != null && !keyword.trim().isEmpty()) {
            qw.and(w -> w.like("content", keyword)
                    .or().like("nickname", keyword)
                    .or().like("username", keyword));
        }
        qw.orderByDesc("create_time");

        var result = commentService.page(pageParam, qw);
        return Result.success(result);
    }

    /**
     * 获取最近评论（社区页面用）
     */
    @GetMapping("/recent")
    public Result<List<Map<String, Object>>> getRecentComments(@RequestParam(defaultValue = "10") int limit) {
        QueryWrapper<Comment> qw = new QueryWrapper<>();
        qw.eq("del_flag", 0);
        qw.isNull("parent_id");
        qw.orderByDesc("create_time");
        qw.last("LIMIT " + limit);

        List<Comment> comments = commentService.list(qw);
        List<Map<String, Object>> result = comments.stream()
                .map(this::commentToMap)
                .collect(Collectors.toList());
        return Result.success(result);
    }

    private void sendCommentNotification(Comment comment, User currentUser) {
        if (comment.getParentId() != null && !comment.getParentId().trim().isEmpty()) {
            Comment parent = commentService.getById(comment.getParentId());
            if (parent != null) {
                notifyUsername(parent.getUsername(), currentUser, "评论收到回复",
                        displayName(currentUser) + " 回复了你的评论");
            }
            return;
        }

        Note note = noteService.getById(comment.getNoteId());
        if (note != null) {
            notifyUsername(note.getCreateBy(), currentUser, "笔记收到新评论",
                    displayName(currentUser) + " 评论了你的笔记：" + shortText(note.getTitle()));
        }
    }

    private void notifyUsername(String username, User actor, String title, String content) {
        if (username == null || username.equals(actor.getUsername())) return;
        User receiver = userService.getOne(new QueryWrapper<User>().eq("username", username));
        if (receiver == null) return;

        Notification notification = new Notification();
        notification.setId(IdUtil.getSnowflakeNextId());
        notification.setUserId(receiver.getId());
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(2);
        notification.setIsRead(0);
        notification.setCreateTime(new Date());
        notificationService.save(notification);
    }

    private String displayName(User user) {
        return user.getNickname() != null && !user.getNickname().trim().isEmpty()
                ? user.getNickname()
                : user.getUsername();
    }

    private String shortText(String text) {
        if (text == null) return "";
        return text.length() > 30 ? text.substring(0, 30) + "..." : text;
    }
}
