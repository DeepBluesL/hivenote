package com.hivenote.controller;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hivenote.common.Result;
import com.hivenote.entity.Post;
import com.hivenote.entity.PostReply;
import com.hivenote.entity.User;
import com.hivenote.service.IPostReplyService;
import com.hivenote.service.IPostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/post")
public class PostController {

    @Autowired
    private IPostService postService;

    @Autowired
    private IPostReplyService postReplyService;

    /**
     * 热门帖子（按浏览+点赞+回复综合排序）
     */
    @GetMapping("/hot")
    public Result<List<Post>> getHotPosts(@RequestParam(defaultValue = "10") int limit) {
        QueryWrapper<Post> qw = new QueryWrapper<>();
        qw.eq("status", 1);
        qw.last("ORDER BY IFNULL(like_count,0)*3 + IFNULL(view_count,0) + IFNULL(reply_count,0)*5 DESC LIMIT " + limit);
        return Result.success(postService.list(qw));
    }

    /**
     * 帖子列表（分页 + 搜索）
     */
    @GetMapping("/list")
    public Result<Object> getList(@RequestParam(defaultValue = "1") int page,
                                  @RequestParam(defaultValue = "10") int size,
                                  String keyword) {
        Page<Post> pageParam = new Page<>(page, size);
        QueryWrapper<Post> qw = new QueryWrapper<>();
        qw.eq("status", 1);
        if (keyword != null && !keyword.trim().isEmpty()) {
            qw.and(w -> w.like("title", keyword).or().like("content", keyword));
        }
        qw.orderByDesc("create_time");
        return Result.success(postService.page(pageParam, qw));
    }

    /**
     * 帖子详情
     */
    @GetMapping("/detail/{id}")
    public Result<Post> getDetail(@PathVariable Long id) {
        Post post = postService.getById(id);
        if (post == null || post.getStatus() == null || post.getStatus() != 1) {
            return Result.error("帖子不存在");
        }
        // 增加浏览量
        post.setViewCount((post.getViewCount() != null ? post.getViewCount() : 0) + 1);
        postService.updateById(post);
        return Result.success(post);
    }

    /**
     * 发布帖子
     */
    @PostMapping("/add")
    public Result<Object> addPost(@RequestHeader("accessToken") String token,
                                  @RequestBody Post post) {
        User currentUser = UserController.sessionMap.get(token);
        if (currentUser == null) {
            return Result.error(401, "请先登录");
        }

        if (post.getTitle() == null || post.getTitle().trim().isEmpty()) {
            return Result.error("标题不能为空");
        }
        if (post.getContent() == null || post.getContent().trim().isEmpty()) {
            return Result.error("内容不能为空");
        }

        post.setId(IdUtil.getSnowflakeNextId());
        post.setUserId(currentUser.getId());
        post.setNickname(currentUser.getNickname() != null ? currentUser.getNickname() : currentUser.getUsername());
        post.setAvatar(currentUser.getAvatar());
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setReplyCount(0);
        post.setStatus(1);
        post.setCreateTime(new Date());
        post.setUpdateTime(new Date());

        postService.save(post);
        return Result.success("发布成功");
    }

    /**
     * 删除帖子（作者或管理员）
     */
    @PostMapping("/delete/{id}")
    public Result<Object> deletePost(@PathVariable Long id,
                                     @RequestHeader("accessToken") String token) {
        User currentUser = UserController.sessionMap.get(token);
        if (currentUser == null) {
            return Result.error(401, "请先登录");
        }

        Post post = postService.getById(id);
        if (post == null) {
            return Result.error("帖子不存在");
        }

        boolean isAuthor = post.getUserId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getType() != null && currentUser.getType() == 1;
        if (!isAuthor && !isAdmin) {
            return Result.error(403, "无权删除");
        }

        post.setStatus(0);
        postService.updateById(post);
        return Result.success("删除成功");
    }

    /**
     * 帖子点赞
     */
    @PostMapping("/like/{id}")
    public Result<Object> likePost(@PathVariable Long id,
                                   @RequestHeader("accessToken") String token) {
        User currentUser = UserController.sessionMap.get(token);
        if (currentUser == null) {
            return Result.error(401, "请先登录");
        }

        Post post = postService.getById(id);
        if (post == null) {
            return Result.error("帖子不存在");
        }

        post.setLikeCount((post.getLikeCount() != null ? post.getLikeCount() : 0) + 1);
        postService.updateById(post);
        return Result.success(post.getLikeCount());
    }

    // ========== 回复相关 ==========

    /**
     * 获取帖子回复列表
     */
    @GetMapping("/reply/list/{postId}")
    public Result<List<PostReply>> getReplies(@PathVariable Long postId) {
        QueryWrapper<PostReply> qw = new QueryWrapper<>();
        qw.eq("post_id", postId);
        qw.eq("status", 1);
        qw.orderByAsc("create_time");
        return Result.success(postReplyService.list(qw));
    }

    /**
     * 发布回复
     */
    @PostMapping("/reply/add")
    public Result<Object> addReply(@RequestHeader("accessToken") String token,
                                   @RequestBody PostReply reply) {
        User currentUser = UserController.sessionMap.get(token);
        if (currentUser == null) {
            return Result.error(401, "请先登录");
        }

        if (reply.getPostId() == null) {
            return Result.error("帖子ID不能为空");
        }
        if (reply.getContent() == null || reply.getContent().trim().isEmpty()) {
            return Result.error("回复内容不能为空");
        }

        Post post = postService.getById(reply.getPostId());
        if (post == null || post.getStatus() != 1) {
            return Result.error("帖子不存在");
        }

        reply.setId(IdUtil.getSnowflakeNextId());
        reply.setUserId(currentUser.getId());
        reply.setNickname(currentUser.getNickname() != null ? currentUser.getNickname() : currentUser.getUsername());
        reply.setAvatar(currentUser.getAvatar());
        reply.setUserType(currentUser.getType() != null ? currentUser.getType() : 0);
        reply.setStatus(1);
        reply.setCreateTime(new Date());
        postReplyService.save(reply);

        // 更新帖子回复数
        post.setReplyCount((post.getReplyCount() != null ? post.getReplyCount() : 0) + 1);
        post.setUpdateTime(new Date());
        postService.updateById(post);

        return Result.success("回复成功");
    }

    /**
     * 删除回复（作者或管理员）
     */
    @PostMapping("/reply/delete/{id}")
    public Result<Object> deleteReply(@PathVariable Long id,
                                      @RequestHeader("accessToken") String token) {
        User currentUser = UserController.sessionMap.get(token);
        if (currentUser == null) {
            return Result.error(401, "请先登录");
        }

        PostReply reply = postReplyService.getById(id);
        if (reply == null) {
            return Result.error("回复不存在");
        }

        boolean isAuthor = reply.getUserId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getType() != null && currentUser.getType() == 1;
        if (!isAuthor && !isAdmin) {
            return Result.error(403, "无权删除");
        }

        reply.setStatus(0);
        postReplyService.updateById(reply);

        // 减少帖子回复数
        Post post = postService.getById(reply.getPostId());
        if (post != null) {
            post.setReplyCount(Math.max(0, (post.getReplyCount() != null ? post.getReplyCount() : 0) - 1));
            postService.updateById(post);
        }

        return Result.success("删除成功");
    }

    /**
     * 上传帖子图片
     */
    @PostMapping("/uploadImage")
    public Result<Object> uploadImage() {
        // 复用 FileController 的 uploadAvatar 逻辑，前端直接调用 /file/uploadAvatar
        return Result.error("请使用 /file/uploadPostImage 接口");
    }
}
