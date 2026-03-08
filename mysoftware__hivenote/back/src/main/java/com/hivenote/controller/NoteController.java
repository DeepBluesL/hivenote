package com.hivenote.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hivenote.common.Result;
import com.hivenote.entity.Note;
import com.hivenote.entity.User;
import com.hivenote.service.INoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/note")
public class NoteController {

    @Autowired
    private INoteService noteService;

    /**
     * 1. 获取笔记列表
     * 前端参数: page, size, keyword, category, sortBy
     * keyword 同时搜索 title 和 summary
     */
    @GetMapping("/list")
    public Result<IPage<Note>> getList(@RequestParam(defaultValue = "1") int page,
                                       @RequestParam(defaultValue = "12") int size,
                                       String title,
                                       String keyword,
                                       String category,
                                       @RequestParam(defaultValue = "newest") String sortBy) {
        Page<Note> pageParam = new Page<>(page, size);
        
        QueryWrapper<Note> qw = new QueryWrapper<>();

        // keyword 支持同时搜索标题、简介、上传者
        String searchTerm = StrUtil.isNotBlank(keyword) ? keyword : title;
        if (StrUtil.isNotBlank(searchTerm)) {
            qw.and(w -> w.like("title", searchTerm)
                    .or().like("summary", searchTerm)
                    .or().like("create_by", searchTerm));
        }
        if (StrUtil.isNotBlank(category)) {
            qw.eq("category", category);
        }
        qw.eq("status", 1);
        qw.eq("del_flag", 0);

        switch (sortBy) {
            case "popular":
                qw.orderByDesc("view_count");
                break;
            case "downloads":
                qw.orderByDesc("download_count");
                break;
            case "likes":
                qw.orderByDesc("like_count");
                break;
            default:
                qw.orderByDesc("create_time");
                break;
        }

        IPage<Note> result = noteService.page(pageParam, qw);
        return Result.success(result);
    }

    /**
     * 热门笔记 (社区页面用)
     */
    @GetMapping("/hot")
    public Result<java.util.List<Note>> getHotNotes(@RequestParam(defaultValue = "10") int limit) {
        QueryWrapper<Note> qw = new QueryWrapper<>();
        qw.eq("status", 1);
        qw.eq("del_flag", 0);
        qw.orderByDesc("view_count");
        qw.last("LIMIT " + limit);
        return Result.success(noteService.list(qw));
    }

    /**
     * 最新笔记 (社区页面用)
     */
    @GetMapping("/latest")
    public Result<java.util.List<Note>> getLatestNotes(@RequestParam(defaultValue = "10") int limit) {
        QueryWrapper<Note> qw = new QueryWrapper<>();
        qw.eq("status", 1);
        qw.eq("del_flag", 0);
        qw.orderByDesc("create_time");
        qw.last("LIMIT " + limit);
        return Result.success(noteService.list(qw));
    }

    /**
     * 社区统计数据
     */
    @GetMapping("/stats")
    public Result<java.util.Map<String, Object>> getStats() {
        QueryWrapper<Note> qw = new QueryWrapper<>();
        qw.eq("status", 1);
        qw.eq("del_flag", 0);

        long totalNotes = noteService.count(qw);

        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalNotes", totalNotes);
        return Result.success(stats);
    }

    /**
     * 2. 获取详情
     */
    @GetMapping("/detail/{id}")
    public Result<Note> getDetail(@PathVariable String id) {
        System.out.println("=== 获取笔记详情 ===");
        System.out.println("笔记ID: " + id);
        
        Note note = noteService.getById(id);
        if (note == null) {
            System.out.println("错误: 笔记不存在");
            return Result.error("笔记不存在");
        }
        
        System.out.println("标题: " + note.getTitle());
        System.out.println("分类: " + note.getCategory());
        System.out.println("创建人: " + note.getCreateBy());
        System.out.println("当前浏览量: " + note.getViewCount());
        
        // 增加浏览量
        Integer viewCount = note.getViewCount() != null ? note.getViewCount() : 0;
        note.setViewCount(viewCount + 1);
        noteService.updateById(note);
        
        System.out.println("更新后浏览量: " + note.getViewCount());
        System.out.println("==================");
        
        return Result.success(note);
    }

    /**
     * 3. 发布笔记
     * 前端: { title: '...', category: '...', summary: '...', fileUrl: '...' }
     */
    @PostMapping("/add")
    public Result<Object> add(@RequestBody Note note, @RequestHeader("accessToken") String token) {
        System.out.println("=== 发布笔记调试信息 ===");
        System.out.println("标题: " + note.getTitle());
        System.out.println("分类: " + note.getCategory());
        System.out.println("简介: " + note.getSummary());
        System.out.println("文件URL: " + note.getFileUrl());
        
        // 从 Token 获取当前登录用户
        User currentUser = UserController.sessionMap.get(token);
        if (currentUser == null) {
            return Result.error(401, "请先登录");
        }

        // 参数校验
        if (note.getTitle() == null || note.getTitle().trim().isEmpty()) {
            return Result.error("标题不能为空");
        }
        if (note.getCategory() == null || note.getCategory().trim().isEmpty()) {
            return Result.error("分类不能为空");
        }
        if (note.getFileUrl() == null || note.getFileUrl().trim().isEmpty()) {
            return Result.error("文件不能为空");
        }

        // 生成ID
        note.setId(cn.hutool.core.util.IdUtil.getSnowflakeNextIdStr());
        
        // 补全信息
        note.setCreateBy(currentUser.getUsername()); // 存储用户名
        note.setViewCount(0);
        note.setDownloadCount(0);
        note.setStatus(0); // 默认待审核(0)，审核通过后改为1
        note.setDelFlag(0); // 未删除
        note.setCreateTime(new java.util.Date());

        System.out.println("生成的笔记ID: " + note.getId());
        System.out.println("创建人: " + note.getCreateBy());
        
        boolean saved = noteService.save(note);
        
        System.out.println("保存结果: " + (saved ? "成功" : "失败"));
        System.out.println("==================");
        
        if (saved) {
            return Result.success("发布成功");
        } else {
            return Result.error("发布失败，请稍后重试");
        }
    }

    /**
     * 4. 获取我的上传列表
     */
    @GetMapping("/myList")
    public Result<java.util.List<Note>> getMyList(@RequestHeader("accessToken") String token) {
        System.out.println("=== 获取我的上传列表 ===");
        
        // 从 Token 获取当前登录用户
        User currentUser = UserController.sessionMap.get(token);
        if (currentUser == null) {
            return Result.error(401, "请先登录");
        }
        
        System.out.println("用户: " + currentUser.getUsername());
        
        // 查询该用户上传的所有笔记
        QueryWrapper<Note> qw = new QueryWrapper<>();
        qw.eq("create_by", currentUser.getUsername());
        qw.eq("del_flag", 0); // 未删除的
        qw.orderByDesc("create_time"); // 按时间倒序
        
        java.util.List<Note> list = noteService.list(qw);
        
        System.out.println("查询到 " + list.size() + " 条笔记");
        System.out.println("==================");
        
        return Result.success(list);
    }
    
    /**
     * 5. 删除笔记（软删除）
     */
    @PostMapping("/delete/{id}")
    public Result<Object> delete(@PathVariable String id, @RequestHeader("accessToken") String token) {
        System.out.println("=== 删除笔记 ===");
        System.out.println("笔记ID: " + id);
        
        // 从 Token 获取当前登录用户
        User currentUser = UserController.sessionMap.get(token);
        if (currentUser == null) {
            return Result.error(401, "请先登录");
        }
        
        Note note = noteService.getById(id);
        if (note == null) {
            return Result.error("笔记不存在");
        }
        
        // 验证是否是本人的笔记
        if (!currentUser.getUsername().equals(note.getCreateBy())) {
            System.out.println("错误: 无权删除他人笔记");
            return Result.error("无权删除他人笔记");
        }
        
        // 软删除：设置 del_flag = 1
        note.setDelFlag(1);
        note.setUpdateBy(currentUser.getUsername());
        note.setUpdateTime(new java.util.Date());
        
        boolean updated = noteService.updateById(note);
        
        System.out.println("删除结果: " + (updated ? "成功" : "失败"));
        System.out.println("==================");
        
        if (updated) {
            return Result.success("删除成功");
        } else {
            return Result.error("删除失败");
        }
    }
    
    /**
     * 6. 增加下载次数
     */
    @PostMapping("/download/{id}")
    public Result<Object> addDownload(@PathVariable String id) {
        System.out.println("=== 增加下载次数 ===");
        System.out.println("笔记ID: " + id);
        
        Note note = noteService.getById(id);
        if (note != null) {
            Integer downloadCount = note.getDownloadCount() != null ? note.getDownloadCount() : 0;
            System.out.println("当前下载量: " + downloadCount);
            
            note.setDownloadCount(downloadCount + 1);
            noteService.updateById(note);
            
            System.out.println("更新后下载量: " + note.getDownloadCount());
            System.out.println("==================");
            
            return Result.success("下载次数已更新");
        }
        
        System.out.println("错误: 笔记不存在");
        System.out.println("==================");
        return Result.error("笔记不存在");
    }
    
    /**
     * 7. 管理员获取笔记列表（包含所有状态）
     */
    @GetMapping("/admin/list")
    public Result<IPage<Note>> getAdminList(@RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "10") int size,
                                            Integer status,
                                            String category,
                                            String title,
                                            @RequestHeader("accessToken") String token) {
        System.out.println("=== 管理员查询笔记列表 ===");
        
        // 验证管理员权限
        User currentUser = UserController.sessionMap.get(token);
        if (currentUser == null) {
            return Result.error(401, "请先登录");
        }
        if (currentUser.getType() == null || currentUser.getType() != 1) {
            return Result.error(403, "无权访问");
        }
        
        System.out.println("页码: " + page);
        System.out.println("每页数量: " + size);
        System.out.println("状态筛选: " + status);
        System.out.println("分类筛选: " + category);
        System.out.println("标题关键词: " + title);
        
        // 构造分页对象
        Page<Note> pageParam = new Page<>(page, size);
        
        // 构造查询条件
        QueryWrapper<Note> qw = new QueryWrapper<>();
        if (status != null) {
            qw.eq("status", status);
        }
        if (StrUtil.isNotBlank(category)) {
            qw.eq("category", category);
        }
        if (StrUtil.isNotBlank(title)) {
            qw.like("title", title);
        }
        qw.eq("del_flag", 0); // 未删除的
        qw.orderByDesc("create_time"); // 按时间倒序

        // 执行查询
        IPage<Note> result = noteService.page(pageParam, qw);
        
        System.out.println("查询结果数量: " + result.getRecords().size());
        System.out.println("总记录数: " + result.getTotal());
        System.out.println("==================");
        
        return Result.success(result);
    }
    
    /**
     * 8. 审核通过笔记
     */
    @PostMapping("/admin/approve/{id}")
    public Result<Object> approveNote(@PathVariable String id,
                                      @RequestHeader("accessToken") String token) {
        System.out.println("=== 审核通过笔记 ===");
        System.out.println("笔记ID: " + id);
        
        // 验证管理员权限
        User currentUser = UserController.sessionMap.get(token);
        if (currentUser == null) {
            return Result.error(401, "请先登录");
        }
        if (currentUser.getType() == null || currentUser.getType() != 1) {
            return Result.error(403, "无权操作");
        }
        
        Note note = noteService.getById(id);
        if (note == null) {
            System.out.println("错误: 笔记不存在");
            return Result.error("笔记不存在");
        }
        
        // 更新状态为通过(1)
        note.setStatus(1);
        note.setUpdateBy(currentUser.getUsername());
        note.setUpdateTime(new java.util.Date());
        
        boolean updated = noteService.updateById(note);
        
        System.out.println("审核结果: " + (updated ? "成功" : "失败"));
        System.out.println("==================");
        
        if (updated) {
            return Result.success("审核通过");
        } else {
            return Result.error("操作失败");
        }
    }
    
    /**
     * 9. 拒绝笔记
     */
    @PostMapping("/admin/reject/{id}")
    public Result<Object> rejectNote(@PathVariable String id,
                                     @RequestHeader("accessToken") String token) {
        System.out.println("=== 拒绝笔记 ===");
        System.out.println("笔记ID: " + id);
        
        // 验证管理员权限
        User currentUser = UserController.sessionMap.get(token);
        if (currentUser == null) {
            return Result.error(401, "请先登录");
        }
        if (currentUser.getType() == null || currentUser.getType() != 1) {
            return Result.error(403, "无权操作");
        }
        
        Note note = noteService.getById(id);
        if (note == null) {
            System.out.println("错误: 笔记不存在");
            return Result.error("笔记不存在");
        }
        
        // 更新状态为拒绝(2)
        note.setStatus(2);
        note.setUpdateBy(currentUser.getUsername());
        note.setUpdateTime(new java.util.Date());
        
        boolean updated = noteService.updateById(note);
        
        System.out.println("拒绝结果: " + (updated ? "成功" : "失败"));
        System.out.println("==================");
        
        if (updated) {
            return Result.success("已拒绝");
        } else {
            return Result.error("操作失败");
        }
    }
}