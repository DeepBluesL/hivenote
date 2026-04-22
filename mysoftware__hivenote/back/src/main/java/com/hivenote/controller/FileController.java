package com.hivenote.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.hivenote.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/file")
public class FileController {

    // 读取配置文件里的路径: D:/hivenote/files/
    @Value("${file.upload-path}")
    private String uploadPath;

    /**
     * 文件上传
     * 前端: FormData 格式，key='file'
     */
    @PostMapping("/upload")
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file) throws IOException {
        System.out.println("=== 文件上传调试信息 ===");
        
        if (file.isEmpty()) {
            System.out.println("错误: 文件为空");
            return Result.error("文件不能为空");
        }

        // 1. 验证文件类型
        String originalFilename = file.getOriginalFilename();
        String suffix = FileUtil.getSuffix(originalFilename).toLowerCase();
        
        // 只允许 pdf, mp4, docx, zip, 图片格式
        if (!suffix.matches("pdf|mp4|docx|zip|png|jpg|jpeg|gif|webp")) {
            System.out.println("错误: 不支持的文件类型 - " + suffix);
            return Result.error("只支持上传 PDF、MP4、DOCX、ZIP、PNG、JPG、GIF、WEBP 格式的文件");
        }
        
        // 2. 验证文件大小（50MB）
        long maxSize = 50 * 1024 * 1024; // 50MB
        if (file.getSize() > maxSize) {
            System.out.println("错误: 文件过大 - " + file.getSize() + " bytes");
            return Result.error("文件大小不能超过 50MB");
        }
        
        // 3. 生成文件名: 20251024_uuid.pdf
        String fileName = DateUtil.format(new Date(), "yyyyMMdd") + "_" + IdUtil.simpleUUID() + "." + suffix;

        System.out.println("原始文件名: " + originalFilename);
        System.out.println("文件类型: " + suffix);
        System.out.println("文件大小: " + file.getSize() + " bytes");
        System.out.println("生成文件名: " + fileName);
        System.out.println("保存路径: " + uploadPath);

        // 4. 确保目录存在
        if (!FileUtil.exist(uploadPath)) {
            FileUtil.mkdir(uploadPath);
            System.out.println("创建上传目录: " + uploadPath);
        }

        // 5. 保存文件到磁盘
        String fullPath = uploadPath + fileName;
        file.transferTo(new File(fullPath));
        System.out.println("文件保存成功: " + fullPath);

        // 6. 返回访问 URL 和下载 URL
        String fileUrl = "http://localhost:8080/zwz/file/view/" + fileName;
        String downloadUrl = "http://localhost:8080/zwz/file/download/" + fileName;
        
        System.out.println("文件访问URL: " + fileUrl);
        System.out.println("文件下载URL: " + downloadUrl);
        System.out.println("==================");
        
        Map<String, String> map = new HashMap<>();
        map.put("url", fileUrl);
        map.put("downloadUrl", downloadUrl);
        map.put("name", originalFilename);
        map.put("fileName", fileName);
        
        return Result.success(map);
    }

    /**
     * 文件预览
     * URL: /zwz/file/view/{fileName}
     */
    @GetMapping("/view/{fileName}")
    public void view(@PathVariable String fileName, HttpServletResponse response) throws IOException {
        File file = new File(uploadPath + fileName);
        if (!file.exists()) {
            System.out.println("文件不存在: " + uploadPath + fileName);
            return;
        }

        // 根据文件扩展名设置正确的 Content-Type，使图片等资源可内联显示
        String ext = FileUtil.getSuffix(fileName).toLowerCase();
        String contentType;
        switch (ext) {
            case "png":  contentType = "image/png"; break;
            case "jpg":
            case "jpeg": contentType = "image/jpeg"; break;
            case "gif":  contentType = "image/gif"; break;
            case "webp": contentType = "image/webp"; break;
            case "pdf":  contentType = "application/pdf"; break;
            case "mp4":  contentType = "video/mp4"; break;
            default:     contentType = "application/octet-stream"; break;
        }
        response.setContentType(contentType);
        
        // 利用 Hutool 快速写出文件流
        byte[] bytes = FileUtil.readBytes(file);
        response.getOutputStream().write(bytes);
    }
    
    /**
     * 文件下载
     * URL: /zwz/file/download/{fileName}
     * 可选参数: originalName - 原始文件名
     */
    @GetMapping("/download/{fileName}")
    public void download(@PathVariable String fileName, 
                        @RequestParam(required = false) String originalName,
                        HttpServletResponse response) throws IOException {
        System.out.println("=== 文件下载 ===");
        System.out.println("文件名: " + fileName);
        System.out.println("原始文件名: " + originalName);
        
        File file = new File(uploadPath + fileName);
        if (!file.exists()) {
            System.out.println("错误: 文件不存在 - " + uploadPath + fileName);
            response.setStatus(404);
            return;
        }

        System.out.println("文件路径: " + file.getAbsolutePath());
        System.out.println("文件大小: " + file.length() + " bytes");
        
        // 使用原始文件名，如果没有则使用服务器文件名
        String downloadName = (originalName != null && !originalName.isEmpty()) ? originalName : fileName;
        
        // 设置响应头，强制下载
        response.setContentType("application/octet-stream");
        response.setCharacterEncoding("UTF-8");
        
        // 设置 Content-Disposition 头，告诉浏览器下载文件
        // 使用 URLEncoder 编码文件名，支持中文
        String encodedFileName = java.net.URLEncoder.encode(downloadName, "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName);
        response.setHeader("Content-Length", String.valueOf(file.length()));
        
        System.out.println("下载文件名: " + downloadName);
        System.out.println("开始传输文件...");
        
        // 读取文件并写入响应流
        byte[] bytes = FileUtil.readBytes(file);
        response.getOutputStream().write(bytes);
        response.getOutputStream().flush();
        
        System.out.println("文件下载完成");
        System.out.println("==================");
    }
}