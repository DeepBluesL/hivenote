-- 学校与隐私设置功能 - 数据库迁移脚本
-- 请在 hivenote 数据库中执行

-- 1. 用户表增加字段：学校、是否公开邮箱、是否公开学号
ALTER TABLE a_user ADD COLUMN school VARCHAR(100) DEFAULT NULL COMMENT '所属学校';
ALTER TABLE a_user ADD COLUMN show_email INT DEFAULT 1 COMMENT '是否公开展示邮箱 (0:不公开 1:公开)';
ALTER TABLE a_user ADD COLUMN show_student_id INT DEFAULT 1 COMMENT '是否公开展示学号 (0:不公开 1:公开)';

-- 2. 笔记表增加字段：上传者所属学校
ALTER TABLE t_note ADD COLUMN school VARCHAR(100) DEFAULT NULL COMMENT '上传者所属学校';
