package com.hivenote.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 评论实体类
 */
@Data
@TableName("t_comment")
public class Comment implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 评论ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 笔记ID
     */
    private String noteId;

    /**
     * 父评论ID (回复功能)
     */
    private String parentId;

    /**
     * 回复目标用户名
     */
    private String replyTo;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 评论人用户名
     */
    private String username;

    /**
     * 评论人昵称
     */
    private String nickname;

    /**
     * 评论时间
     */
    private Date createTime;

    /**
     * 删除标记 (0:正常 1:删除)
     */
    private Integer delFlag;
}
