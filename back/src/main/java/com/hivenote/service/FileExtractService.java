package com.hivenote.service;

import com.hivenote.common.Result;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@Service
@RestController
public class FileExtractService {

    /**
     * 提取文件内容接口
     */
    @PostMapping("/file/extract")
    public Result<String> extractFileContent(@RequestBody Map<String, String> request) {
        String fileUrl = request.get("fileUrl");
        
        if (fileUrl == null || fileUrl.trim().isEmpty()) {
            return Result.error("文件地址不能为空");
        }
        
        File tempFile = null;
        
        try {
            // 1. 下载文件到临时目录
            String tempDir = System.getProperty("java.io.tmpdir");
            String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            fileName = fileName.replaceAll("[^a-zA-Z0-9.-]", "_");
            String tempFilePath = tempDir + File.separator + UUID.randomUUID() + "_" + fileName;
            tempFile = new File(tempFilePath);
            
            System.out.println("下载文件: " + fileUrl + " -> " + tempFilePath);
            downloadFile(fileUrl, tempFilePath);
            
            // 2. 根据文件类型提取文本
            String extractedText = extractTextFromFile(tempFilePath);
            
            // 3. 添加文件基本信息
            StringBuilder result = new StringBuilder();
            result.append("【文件信息】\n");
            result.append("文件名: ").append(fileName).append("\n");
            result.append("文件大小: ").append(formatFileSize(Files.size(Paths.get(tempFilePath)))).append("\n");
            result.append("文件类型: ").append(getFileExtension(fileName)).append("\n");
            result.append("=".repeat(60)).append("\n\n");
            result.append("【文件内容】\n");
            result.append(extractedText);
            
            return Result.success(result.toString());
            
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("文件内容提取失败: " + e.getMessage());
        } finally {
            // 4. 删除临时文件
            if (tempFile != null && tempFile.exists()) {
                try {
                    Files.deleteIfExists(tempFile.toPath());
                    System.out.println("临时文件已删除: " + tempFile.getPath());
                } catch (IOException e) {
                    System.err.println("删除临时文件失败: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * 下载文件
     */
    private void downloadFile(String fileUrl, String destinationPath) throws IOException {
        URL url = new URL(fileUrl);
        try (InputStream in = url.openStream();
             OutputStream out = new FileOutputStream(destinationPath)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }
    
    /**
     * 根据文件类型提取文本
     */
    private String extractTextFromFile(String filePath) throws IOException {
        String fileName = filePath.toLowerCase();
        String extension = getFileExtension(fileName);
        
        System.out.println("提取文件内容: " + fileName + ", 类型: " + extension);
        
        try {
            switch (extension) {
                case "txt":
                case "java":
                case "py":
                case "js":
                case "html":
                case "css":
                case "xml":
                case "json":
                case "md":
                    // 文本文件直接读取
                    return readTextFile(filePath);
                    
                case "pdf":
                    // PDF文件使用PDFBox提取
                    return extractFromPDF(filePath);
                    
                case "docx":
                case "doc":
                case "pptx":
                case "xlsx":
                    // Office文件 - 提示需要额外配置
                    return "[Office文件] " + extension + " 格式需要完整POI支持。\n" +
                           "请确保已添加所有POI依赖。\n" +
                           "文件名: " + new File(filePath).getName();
                    
                default:
                    return "[不支持的文件格式] 无法提取 " + extension + " 格式的内容。\n" +
                           "文件预览: " + new File(filePath).getName();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "[提取失败] " + e.getMessage() + "\n文件: " + new File(filePath).getName();
        }
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            return fileName.substring(i + 1);
        }
        return "";
    }
    
    /**
     * 格式化文件大小
     */
    private String formatFileSize(long size) {
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.2f KB", size / 1024.0);
        if (size < 1024 * 1024 * 1024) return String.format("%.2f MB", size / (1024.0 * 1024.0));
        return String.format("%.2f GB", size / (1024.0 * 1024.0 * 1024.0));
    }
    
    /**
     * 读取文本文件
     */
    private String readTextFile(String filePath) throws IOException {
        return Files.readString(Paths.get(filePath));
    }
    
    /**
     * 从PDF提取文本
     */
    private String extractFromPDF(String filePath) throws IOException {
        try (PDDocument document = PDDocument.load(new File(filePath))) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            
            StringBuilder result = new StringBuilder();
            result.append("PDF文档信息:\n");
            result.append("页数: ").append(document.getNumberOfPages()).append("\n");
            result.append("是否加密: ").append(document.isEncrypted() ? "是" : "否").append("\n\n");
            result.append("提取的文本内容:\n");
            result.append(text);
            
            return result.toString();
        }
    }
}
