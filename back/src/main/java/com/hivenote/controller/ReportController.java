package com.hivenote.controller;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hivenote.common.Result;
import com.hivenote.entity.Comment;
import com.hivenote.entity.ContentReport;
import com.hivenote.entity.Notification;
import com.hivenote.entity.Post;
import com.hivenote.entity.PostReply;
import com.hivenote.entity.User;
import com.hivenote.service.ICommentService;
import com.hivenote.service.IContentReportService;
import com.hivenote.service.INotificationService;
import com.hivenote.service.IPostReplyService;
import com.hivenote.service.IPostService;
import com.hivenote.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/report")
public class ReportController {

    @Autowired
    private IContentReportService reportService;

    @Autowired
    private IUserService userService;

    @Autowired
    private INotificationService notificationService;

    @Autowired
    private IPostService postService;

    @Autowired
    private IPostReplyService postReplyService;

    @Autowired
    private ICommentService commentService;

    @PostMapping("/add")
    public Result<Object> addReport(@RequestHeader("accessToken") String token,
                                    @RequestBody Map<String, Object> params) {
        User currentUser = UserController.sessionMap.get(token);
        if (currentUser == null) {
            return Result.error(401, "请先登录");
        }

        String targetType = value(params.get("targetType")).toUpperCase();
        String targetId = value(params.get("targetId"));
        String reason = value(params.get("reason"));
        if (targetType.isEmpty() || targetId.isEmpty()) {
            return Result.error("举报对象不能为空");
        }
        if (reason.isEmpty()) {
            return Result.error("举报原因不能为空");
        }
        if (!targetType.equals("POST") && !targetType.equals("POST_REPLY") && !targetType.equals("COMMENT")) {
            return Result.error("举报类型不支持");
        }

        ContentReport report = new ContentReport();
        report.setId(IdUtil.getSnowflakeNextId());
        report.setTargetType(targetType);
        report.setTargetId(targetId);
        report.setReporterId(currentUser.getId());
        report.setReporterName(displayName(currentUser));
        report.setReason(reason);
        report.setStatus(0);
        report.setCreateTime(new Date());
        reportService.save(report);

        notifyAdmins(displayName(currentUser) + " 提交了举报：" + targetType + " #" + targetId);
        return Result.success("举报已提交");
    }

    @GetMapping("/admin/list")
    public Result<Object> getAdminReportList(@RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "20") int size,
                                             Integer status,
                                             @RequestHeader("accessToken") String token) {
        User currentUser = UserController.sessionMap.get(token);
        if (!isAdmin(currentUser)) {
            return Result.error(currentUser == null ? 401 : 403, currentUser == null ? "请先登录" : "无权访问");
        }

        Page<ContentReport> pageParam = new Page<>(page, size);
        QueryWrapper<ContentReport> qw = new QueryWrapper<>();
        if (status != null) {
            qw.eq("status", status);
        }
        qw.orderByAsc("status");
        qw.orderByDesc("create_time");
        return Result.success(reportService.page(pageParam, qw));
    }

    @PostMapping("/admin/handle/{id}")
    public Result<Object> handleReport(@PathVariable Long id,
                                       @RequestBody Map<String, Object> params,
                                       @RequestHeader("accessToken") String token) {
        User currentUser = UserController.sessionMap.get(token);
        if (!isAdmin(currentUser)) {
            return Result.error(currentUser == null ? 401 : 403, currentUser == null ? "请先登录" : "无权操作");
        }

        ContentReport report = reportService.getById(id);
        if (report == null) {
            return Result.error("举报不存在");
        }
        if (report.getStatus() != null && report.getStatus() != 0) {
            return Result.error("举报已处理");
        }

        boolean accepted = Boolean.TRUE.equals(params.get("accepted"));
        String handleResult = value(params.get("handleResult"));
        if (accepted) {
            deleteTarget(report);
        }

        report.setStatus(accepted ? 1 : 2);
        report.setHandler(currentUser.getUsername());
        report.setHandleResult(handleResult);
        report.setHandleTime(new Date());
        reportService.updateById(report);

        saveNotification(report.getReporterId(), accepted ? "举报已通过" : "举报已驳回",
                handleResult.isEmpty() ? "你的举报已处理" : handleResult);
        return Result.success(accepted ? "举报已通过并处理内容" : "举报已驳回");
    }

    private void deleteTarget(ContentReport report) {
        if ("POST".equals(report.getTargetType())) {
            Post post = postService.getById(Long.valueOf(report.getTargetId()));
            if (post != null) {
                post.setStatus(0);
                post.setUpdateTime(new Date());
                postService.updateById(post);
            }
        } else if ("POST_REPLY".equals(report.getTargetType())) {
            PostReply reply = postReplyService.getById(Long.valueOf(report.getTargetId()));
            if (reply != null) {
                boolean wasVisible = reply.getStatus() == null || reply.getStatus() == 1;
                reply.setStatus(0);
                postReplyService.updateById(reply);
                if (wasVisible) {
                    Post post = postService.getById(reply.getPostId());
                    if (post != null) {
                        post.setReplyCount(Math.max(0, (post.getReplyCount() != null ? post.getReplyCount() : 0) - 1));
                        postService.updateById(post);
                    }
                }
            }
        } else if ("COMMENT".equals(report.getTargetType())) {
            Comment comment = commentService.getById(report.getTargetId());
            if (comment != null) {
                comment.setDelFlag(1);
                commentService.updateById(comment);
            }
        }
    }

    private void notifyAdmins(String content) {
        List<User> admins = userService.list(new QueryWrapper<User>().eq("type", 1));
        for (User admin : admins) {
            saveNotification(admin.getId(), "收到新的举报", content);
        }
    }

    private void saveNotification(Long userId, String title, String content) {
        if (userId == null) return;
        Notification notification = new Notification();
        notification.setId(IdUtil.getSnowflakeNextId());
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(2);
        notification.setIsRead(0);
        notification.setCreateTime(new Date());
        notificationService.save(notification);
    }

    private boolean isAdmin(User user) {
        return user != null && user.getType() != null && user.getType() == 1;
    }

    private String value(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private String displayName(User user) {
        return user.getNickname() != null && !user.getNickname().trim().isEmpty()
                ? user.getNickname()
                : user.getUsername();
    }
}
