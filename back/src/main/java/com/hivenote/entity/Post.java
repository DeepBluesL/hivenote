package com.hivenote.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("t_post")
public class Post implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String title;

    private String content;

    /**
     * 帖子图片URL（多张用逗号分隔）
     */
    private String images;

    /**
     * 发帖人用户ID
     */
    private Long userId;

    /**
     * 发帖人昵称
     */
    private String nickname;

    /**
     * 发帖人头像
     */
    private String avatar;

    /**
     * 浏览量
     */
    private Integer viewCount;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 回复数
     */
    private Integer replyCount;

    private Integer pinned;

    private Date pinnedTime;

    /**
     * 状态 (1:正常 0:已删除)
     */
    private Integer status;

    private Date createTime;

    private Date updateTime;
}
