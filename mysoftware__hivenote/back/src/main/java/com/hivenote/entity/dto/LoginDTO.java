package com.hivenote.entity.dto;

import lombok.Data;

/**
 * 登录请求 DTO
 */
@Data
public class LoginDTO {
    /**
     * 学号/用户名
     */
    private String username;
    
    /**
     * 密码
     */
    private String password;
}
