package com.hivenote.entity.vo;

import lombok.Data;
import java.util.Date;

/**
 * 用户信息 VO（View Object）
 * 用于返回给前端，不包含敏感信息如密码
 */
@Data
public class UserVO {
    /**
     * 用户ID
     */
    private Long id;
    
    /**
     * 学号/用户名
     */
    private String username;
    
    /**
     * 昵称
     */
    private String nickname;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 头像URL
     */
    private String avatar;
    
    /**
     * 状态 (1:正常 0:禁用)
     */
    private Integer status;
    
    /**
     * 注册时间
     */
    private Date createTime;
}
