-- ============================================
-- HiveNote 数据库迁移脚本
-- 在 MySQL 中逐条执行（已存在的列会报错可忽略）
-- ============================================

-- 1. t_note 表：添加 tags 字段
ALTER TABLE t_note ADD COLUMN tags VARCHAR(500) DEFAULT '' COMMENT '标签，逗号分隔';

-- 2. t_note 表：添加 featured 字段
ALTER TABLE t_note ADD COLUMN featured INT DEFAULT 0 COMMENT '是否推荐 0否 1是';

-- 3. t_note 表：添加 exam_featured 字段
ALTER TABLE t_note ADD COLUMN exam_featured INT DEFAULT 0 COMMENT '是否期末冲刺推荐 0否 1是';

-- 4. t_comment 表：添加 user_type 字段
ALTER TABLE t_comment ADD COLUMN user_type INT DEFAULT 0 COMMENT '评论人类型 0普通用户 1管理员';

-- 5. a_user 表：添加 avatar_banned 字段
ALTER TABLE a_user ADD COLUMN avatar_banned INT DEFAULT 0 COMMENT '头像是否被封禁 0正常 1封禁';

-- 7. a_user table: add Google login fields
ALTER TABLE a_user ADD COLUMN google_id VARCHAR(255) DEFAULT NULL COMMENT 'Google account id';
ALTER TABLE a_user ADD COLUMN google_email VARCHAR(255) DEFAULT NULL COMMENT 'Google email';
CREATE INDEX idx_google_id ON a_user (google_id);

-- 6. 创建通知表
CREATE TABLE IF NOT EXISTS t_notification (
  id BIGINT PRIMARY KEY,
  user_id BIGINT NOT NULL COMMENT '接收通知的用户ID',
  title VARCHAR(200) DEFAULT '' COMMENT '通知标题',
  content VARCHAR(1000) DEFAULT '' COMMENT '通知内容',
  type INT DEFAULT 0 COMMENT '通知类型 0系统 1头像封禁',
  is_read INT DEFAULT 0 COMMENT '是否已读 0未读 1已读',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX idx_user_id (user_id),
  INDEX idx_user_read (user_id, is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统通知表';

-- 8. 创建笔记审核记录表
CREATE TABLE IF NOT EXISTS t_note_review (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  note_id VARCHAR(32) NOT NULL COMMENT '笔记ID',
  reviewer VARCHAR(64) DEFAULT '' COMMENT '审核人或提交人',
  review_type VARCHAR(32) DEFAULT '' COMMENT '审核记录类型',
  ai_suggestion TEXT COMMENT 'AI审核建议',
  reject_reason VARCHAR(1000) DEFAULT '' COMMENT '拒绝原因',
  status INT DEFAULT 0 COMMENT '状态 0未读 1已读',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX idx_note_id (note_id),
  INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='笔记审核记录表';

-- 9. Community interaction enhancements
ALTER TABLE t_post ADD COLUMN pinned INT DEFAULT 0 COMMENT '是否置顶 0否 1是';
ALTER TABLE t_post ADD COLUMN pinned_time DATETIME NULL COMMENT '置顶时间';

CREATE TABLE IF NOT EXISTS t_post_like (
  id BIGINT PRIMARY KEY,
  post_id BIGINT NOT NULL COMMENT '帖子ID',
  user_id BIGINT NOT NULL COMMENT '用户ID',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_post_user (post_id, user_id),
  INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子点赞记录表';

CREATE TABLE IF NOT EXISTS t_report (
  id BIGINT PRIMARY KEY,
  target_type VARCHAR(30) NOT NULL COMMENT '举报对象类型',
  target_id VARCHAR(64) NOT NULL COMMENT '举报对象ID',
  reporter_id BIGINT NOT NULL COMMENT '举报人ID',
  reporter_name VARCHAR(100) DEFAULT '' COMMENT '举报人',
  reason VARCHAR(1000) DEFAULT '' COMMENT '举报原因',
  status INT DEFAULT 0 COMMENT '状态 0待处理 1已通过 2已驳回',
  handler VARCHAR(100) DEFAULT '' COMMENT '处理人',
  handle_result VARCHAR(1000) DEFAULT '' COMMENT '处理说明',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  handle_time DATETIME NULL,
  INDEX idx_status_time (status, create_time),
  INDEX idx_target (target_type, target_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容举报表';
