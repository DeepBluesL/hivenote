package com.hivenote.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("t_post_reply")
public class PostReply implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long postId;

    private String content;

    private Long userId;

    private String nickname;

    private String avatar;

    /**
     * 用户类型 (0:普通用户 1:管理员)
     */
    private Integer userType;

    /**
     * 状态 (1:正常 0:已删除)
     */
    private Integer status;

    private Date createTime;
}
