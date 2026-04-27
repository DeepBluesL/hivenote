package com.hivenote.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

@Data
@TableName("t_note_review")
public class NoteReview {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联的笔记ID，与t_note.id类型一致（实际为字符串数字） */
    private String noteId;

    /** 审核人用户名 */
    private String reviewer;

    /** 审核类型：AI_SUGGESTION 或 MANUAL_REJECT */
    private String reviewType;

    /** AI生成的审核建议（仅AI审核时有值） */
    private String aiSuggestion;

    /** 拒绝原因（审核员填写，最终返回给用户） */
    private String rejectReason;

    /** 状态：0 待查看，1 已查看 */
    private Integer status;

    /** 创建时间 */
    private Date createTime;
}
