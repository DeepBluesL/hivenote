-- ============================================
-- HiveNote 数据库迁移脚本
-- 新增字段：笔记标签、推荐标记、评论用户类型
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
