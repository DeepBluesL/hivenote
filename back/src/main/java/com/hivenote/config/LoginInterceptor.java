package com.hivenote.config;

import cn.hutool.json.JSONUtil;
import com.hivenote.common.Result;
import com.hivenote.controller.UserController;
import com.hivenote.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

/**
 * 登录拦截器
 * 作用：检查请求头中的 accessToken 是否有效
 */
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        
        // 1. 如果是 OPTIONS 请求（跨域预检），直接放行
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        // 2. 从请求头获取 Token
        String token = request.getHeader("accessToken");

        // 3. 校验 Token
        if (token != null && !token.isEmpty()) {
            // 从内存 Map 中查找用户 (对应 UserController 中的 sessionMap)
            User user = UserController.sessionMap.get(token);
            if (user != null) {
                // 登录有效，放行
                return true;
            }
        }

        // 4. 校验失败，拒绝访问，返回 401 JSON
        returnUnauthorized(response);
        return false;
    }

    private void returnUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(401);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=utf-8");
        // 使用 Hutool 工具类将 Result 对象转为 JSON 字符串
        response.getWriter().write(JSONUtil.toJsonStr(Result.error(401, "您尚未登录或登录已过期")));
    }
}