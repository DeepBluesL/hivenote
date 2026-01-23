package com.hivenote.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 全局配置类
 * 包含：跨域配置 + 拦截器注册
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 1. 配置跨域 (CORS)
     * 允许前端 localhost:5173 访问本后端
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")                   // 所有接口
                .allowedOriginPatterns("*")          // 所有来源 (生产环境建议指定具体域名)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 允许的方法
                .allowedHeaders("*")                 // 允许的请求头
                .allowCredentials(true)              // 允许携带 Cookie
                .maxAge(3600);                       // 预检请求缓存时间
    }

    /**
     * 2. 注册拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor())
                .addPathPatterns("/**") // 默认拦截所有路径
                // 配置白名单 (不需要登录就能访问的接口)
                .excludePathPatterns(
                        "/login",            // 旧的登录接口（兼容）
                        "/user/login",       // 新的登录接口
                        "/user/adminLogin",
                        "/user/regist",      // 注册接口
                        "/file/view/**",     // 文件预览（公开）
                        "/file/download/**", // 文件下载（公开）
                        "/note/list",        // 笔记列表（公开访问）
                        "/note/detail/**",   // 笔记详情（公开访问）
                        "/note/download/**", // 增加下载次数（公开）
                        "/error",            // 系统错误页
                        "/doc.html",         // Swagger文档 (如果有)
                        "/webjars/**",
                        "/swagger-resources/**",
                        "/v2/api-docs"
                );
    }
}
