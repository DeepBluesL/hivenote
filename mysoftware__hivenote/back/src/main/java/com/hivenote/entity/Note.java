package com.hivenote.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 笔记实体类
 * 对应表: t_note
 */
@Data
@TableName("t_note")
public class Note implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 笔记ID (使用字符串类型，与数据库表一致)
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 标题
     */
    private String title;

    /**
     * 分类 (CS/SE/Math/GE)
     */
    private String category;

    /**
     * 简介
     */
    private String summary;

    /**
     * 文件下载地址 (对应数据库 file_url)
     */
    private String fileUrl;

    /**
     * 创建人 (对应数据库 create_by，存储用户名)
     */
    private String createBy;

    /**
     * 浏览量
     */
    private Integer viewCount;

    /**
     * 下载量
     */
    private Integer downloadCount;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 收藏数
     */
    private Integer favoriteCount;

    /**
     * 状态 (1:正常 0:审核中)
     */
    private Integer status;

    /**
     * 发布时间
     */
    private Date createTime;
    
    /**
     * 更新人
     */
    private String updateBy;
    
    /**
     * 更新时间
     */
    private Date updateTime;
    
    /**
     * 删除标记 (0:正常 1:删除)
     */
    private Integer delFlag;
}