package com.hivenote.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@TableName("t_report")
public class ContentReport implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String targetType;

    private String targetId;

    private Long reporterId;

    private String reporterName;

    private String reason;

    private Integer status;

    private String handler;

    private String handleResult;

    private Date createTime;

    private Date handleTime;
}
