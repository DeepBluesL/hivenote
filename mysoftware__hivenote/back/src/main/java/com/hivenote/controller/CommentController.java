package com.hivenote.controller;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hivenote.common.Result;
import com.hivenote.controller.UserController;
import com.hivenote.entity.Comment;
import com.hivenote.entity.User;
import com.hivenote.service.ICommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/comment")
public class CommentController {

    @Autowired
    private ICommentService commentService;

    /**
     * 获取某篇笔记的评论列表
     */
    @GetMapping("/getAllByPage")
    public Result<List<Comment>> getList(@RequestParam String noteId) {
        System.out.println("=== 获取评论列表 ===");
        System.out.println("笔记ID: " + noteId);
        
        QueryWrapper<Comment> qw = new QueryWrapper<>();
        qw.eq("note_id", noteId);
        qw.eq("del_flag", 0); // 未删除的
        qw.orderByDesc("create_time"); // 按时间倒序
        
        List<Comment> list = commentService.list(qw);
        
        System.out.println("查询到 " + list.size() + " 条评论");
        System.out.println("==================");
        
        return Result.success(list);
    }

    /**
     * 发表评论
     */
    @PostMapping("/save")
    public Result<Object> save(@RequestBody Comment comment, 
                               @RequestHeader("accessToken") String token) {
        System.out.println("=== 发表评论 ===");
        System.out.println("笔记ID: " + comment.getNoteId());
        System.out.println("评论内容: " + comment.getContent());
        
        // 从 Token 获取当前登录用户
        User currentUser = UserController.sessionMap.get(token);
        if (currentUser == null) {
            return Result.error(401, "请先登录");
        }
        
        // 参数校验
        if (comment.getNoteId() == null || comment.getNoteId().trim().isEmpty()) {
            return Result.error("笔记ID不能为空");
        }
        if (comment.getContent() == null || comment.getContent().trim().isEmpty()) {
            return Result.error("评论内容不能为空");
        }
        
        // 生成ID
        comment.setId(IdUtil.getSnowflakeNextIdStr());
        comment.setUsername(currentUser.getUsername());
        comment.setNickname(currentUser.getNickname());
        comment.setCreateTime(new Date());
        comment.setDelFlag(0);
        
        System.out.println("评论人: " + comment.getNickname());
        
        boolean saved = commentService.save(comment);
        
        System.out.println("保存结果: " + (saved ? "成功" : "失败"));
        System.out.println("==================");
        
        if (saved) {
            return Result.success("评论成功");
        } else {
            return Result.error("评论失败");
        }
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/del/{id}")
    public Result<Object> delete(@PathVariable String id, 
                                 @RequestHeader("accessToken") String token) {
        System.out.println("=== 删除评论 ===");
        System.out.println("评论ID: " + id);
        
        // 从 Token 获取当前登录用户
        User currentUser = UserController.sessionMap.get(token);
        if (currentUser == null) {
            return Result.error(401, "请先登录");
        }
        
        Comment comment = commentService.getById(id);
        if (comment == null) {
            return Result.error("评论不存在");
        }
        
        // 验证是否是本人的评论
        if (!currentUser.getUsername().equals(comment.getUsername())) {
            System.out.println("错误: 无权删除他人评论");
            return Result.error("无权删除他人评论");
        }
        
        // 软删除
        comment.setDelFlag(1);
        boolean updated = commentService.updateById(comment);
        
        System.out.println("删除结果: " + (updated ? "成功" : "失败"));
        System.out.println("==================");
        
        if (updated) {
            return Result.success("删除成功");
        } else {
            return Result.error("删除失败");
        }
    }
}
