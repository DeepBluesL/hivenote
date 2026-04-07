package com.hivenote.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("t_notification")
public class Notification implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 接收通知的用户ID
     */
    private Long userId;

    private String title;

    private String content;

    /**
     * 通知类型 (0:系统通知 1:头像封禁 2:其他)
     */
    private Integer type;

    /**
     * 是否已读 (0:未读 1:已读)
     */
    private Integer isRead;

    private Date createTime;
}
