-- Cover Image Feature - Database Migration Script
-- Execute in the hivenote database

-- Add cover_url column to t_note table
ALTER TABLE `t_note` ADD COLUMN `cover_url` varchar(500) DEFAULT NULL COMMENT '封面图片地址' AFTER `file_url`;
