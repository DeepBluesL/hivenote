package com.hivenote.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户实体类
 * 对应表: t_user
 */
@Data
@TableName("a_user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID (雪花算法生成)
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;


    /**
     * 学号/用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

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
     * 用户类型 (0:普通用户 1:管理员)
     */
    private Integer type;

    /**
     * 状态 (0:正常 -1:禁用)
     */
    private Integer status;

    /**
     * 学校
     */
    private String school;

    /**
     * 是否公开展示邮箱 (0:不公开 1:公开，默认1)
     */
    private Integer showEmail;

    /**
     * 是否公开展示学号 (0:不公开 1:公开，默认1)
     */
    private Integer showStudentId;

    /**
     * 头像是否被封禁 (0:正常 1:已封禁)
     */
    private Integer avatarBanned;

    /**
     * 注册时间
     * MyBatis-Plus 会自动将数据库的 create_time 映射为 createTime
     */
    private Date createTime;
}