-- 学校与隐私设置功能 - 数据库迁移脚本
-- 请在 hivenote 数据库中执行

-- 1. 数据库：a_user 表新增 3 列
ALTER TABLE hivenote.a_user
  ADD COLUMN school VARCHAR(255) DEFAULT NULL AFTER status,
  ADD COLUMN show_email INT DEFAULT 1 AFTER school,
  ADD COLUMN show_student_id INT DEFAULT 1 AFTER show_email;

-- 2. 数据库：t_note 表新增 1 列
ALTER TABLE hivenote.t_note
  ADD COLUMN school VARCHAR(255) DEFAULT NULL AFTER create_by;
