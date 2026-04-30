package com.hivenote.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * 数据库自动迁移：启动时检查并补齐缺失的列和表
 */
@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private DataSource dataSource;

    @Override
    public void run(String... args) {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            System.out.println("=== 数据库自动迁移开始 ===");

            // a_user 表: avatar_banned
            addColumnIfNotExists(conn, stmt, "a_user", "avatar_banned",
                    "ALTER TABLE a_user ADD COLUMN avatar_banned INT DEFAULT 0 COMMENT '头像是否被封禁 0正常 1封禁'");

            // a_user table: Google login fields
            addColumnIfNotExists(conn, stmt, "a_user", "google_id",
                    "ALTER TABLE a_user ADD COLUMN google_id VARCHAR(255) DEFAULT NULL COMMENT 'Google account id'");
            addColumnIfNotExists(conn, stmt, "a_user", "google_email",
                    "ALTER TABLE a_user ADD COLUMN google_email VARCHAR(255) DEFAULT NULL COMMENT 'Google email'");

            // t_note 表: tags, featured, exam_featured
            addColumnIfNotExists(conn, stmt, "t_note", "tags",
                    "ALTER TABLE t_note ADD COLUMN tags VARCHAR(500) DEFAULT '' COMMENT '标签，逗号分隔'");
            addColumnIfNotExists(conn, stmt, "t_note", "featured",
                    "ALTER TABLE t_note ADD COLUMN featured INT DEFAULT 0 COMMENT '是否推荐 0否 1是'");
            addColumnIfNotExists(conn, stmt, "t_note", "exam_featured",
                    "ALTER TABLE t_note ADD COLUMN exam_featured INT DEFAULT 0 COMMENT '是否期末冲刺推荐 0否 1是'");

            // t_comment 表: user_type
            addColumnIfNotExists(conn, stmt, "t_comment", "user_type",
                    "ALTER TABLE t_comment ADD COLUMN user_type INT DEFAULT 0 COMMENT '评论人类型 0普通用户 1管理员'");

            // 创建 t_notification 表
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS t_notification (" +
                    "  id BIGINT PRIMARY KEY," +
                    "  user_id BIGINT NOT NULL COMMENT '接收通知的用户ID'," +
                    "  title VARCHAR(200) DEFAULT '' COMMENT '通知标题'," +
                    "  content VARCHAR(1000) DEFAULT '' COMMENT '通知内容'," +
                    "  type INT DEFAULT 0 COMMENT '通知类型 0系统 1头像封禁'," +
                    "  is_read INT DEFAULT 0 COMMENT '是否已读 0未读 1已读'," +
                    "  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'," +
                    "  INDEX idx_user_id (user_id)," +
                    "  INDEX idx_user_read (user_id, is_read)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统通知表'"
            );
            System.out.println("[OK] t_notification 表已就绪");

            // 创建 t_post 帖子表
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS t_post (" +
                    "  id BIGINT PRIMARY KEY," +
                    "  title VARCHAR(200) NOT NULL COMMENT '帖子标题'," +
                    "  content TEXT COMMENT '帖子内容'," +
                    "  images VARCHAR(2000) DEFAULT '' COMMENT '图片URL，逗号分隔'," +
                    "  user_id BIGINT NOT NULL COMMENT '发帖人ID'," +
                    "  nickname VARCHAR(100) DEFAULT '' COMMENT '发帖人昵称'," +
                    "  avatar VARCHAR(500) DEFAULT '' COMMENT '发帖人头像'," +
                    "  view_count INT DEFAULT 0 COMMENT '浏览量'," +
                    "  like_count INT DEFAULT 0 COMMENT '点赞数'," +
                    "  reply_count INT DEFAULT 0 COMMENT '回复数'," +
                    "  pinned INT DEFAULT 0 COMMENT '是否置顶 0否 1是'," +
                    "  pinned_time DATETIME NULL COMMENT '置顶时间'," +
                    "  status INT DEFAULT 1 COMMENT '状态 1正常 0删除'," +
                    "  create_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                    "  INDEX idx_user_id (user_id)," +
                    "  INDEX idx_status_time (status, create_time)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='讨论帖子表'"
            );
            System.out.println("[OK] t_post 表已就绪");
            addColumnIfNotExists(conn, stmt, "t_post", "pinned",
                    "ALTER TABLE t_post ADD COLUMN pinned INT DEFAULT 0 COMMENT '是否置顶 0否 1是'");
            addColumnIfNotExists(conn, stmt, "t_post", "pinned_time",
                    "ALTER TABLE t_post ADD COLUMN pinned_time DATETIME NULL COMMENT '置顶时间'");

            // 创建 t_post_reply 帖子回复表
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS t_post_reply (" +
                    "  id BIGINT PRIMARY KEY," +
                    "  post_id BIGINT NOT NULL COMMENT '帖子ID'," +
                    "  content TEXT COMMENT '回复内容'," +
                    "  user_id BIGINT NOT NULL COMMENT '回复人ID'," +
                    "  nickname VARCHAR(100) DEFAULT '' COMMENT '回复人昵称'," +
                    "  avatar VARCHAR(500) DEFAULT '' COMMENT '回复人头像'," +
                    "  user_type INT DEFAULT 0 COMMENT '用户类型 0普通 1管理员'," +
                    "  status INT DEFAULT 1 COMMENT '状态 1正常 0删除'," +
                    "  create_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "  INDEX idx_post_id (post_id)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子回复表'"
            );
            System.out.println("[OK] t_post_reply 表已就绪");

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS t_post_like (" +
                    "  id BIGINT PRIMARY KEY," +
                    "  post_id BIGINT NOT NULL COMMENT '帖子ID'," +
                    "  user_id BIGINT NOT NULL COMMENT '用户ID'," +
                    "  create_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "  UNIQUE KEY uk_post_user (post_id, user_id)," +
                    "  INDEX idx_user_id (user_id)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子点赞记录表'"
            );
            System.out.println("[OK] t_post_like 表已就绪");

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS t_report (" +
                    "  id BIGINT PRIMARY KEY," +
                    "  target_type VARCHAR(30) NOT NULL COMMENT '举报对象类型'," +
                    "  target_id VARCHAR(64) NOT NULL COMMENT '举报对象ID'," +
                    "  reporter_id BIGINT NOT NULL COMMENT '举报人ID'," +
                    "  reporter_name VARCHAR(100) DEFAULT '' COMMENT '举报人'," +
                    "  reason VARCHAR(1000) DEFAULT '' COMMENT '举报原因'," +
                    "  status INT DEFAULT 0 COMMENT '状态 0待处理 1已通过 2已驳回'," +
                    "  handler VARCHAR(100) DEFAULT '' COMMENT '处理人'," +
                    "  handle_result VARCHAR(1000) DEFAULT '' COMMENT '处理说明'," +
                    "  create_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "  handle_time DATETIME NULL," +
                    "  INDEX idx_status_time (status, create_time)," +
                    "  INDEX idx_target (target_type, target_id)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内容举报表'"
            );
            System.out.println("[OK] t_report 表已就绪");

            // 创建 t_note_review 审核记录表
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS t_note_review (" +
                    "  id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                    "  note_id VARCHAR(32) NOT NULL COMMENT '笔记ID'," +
                    "  reviewer VARCHAR(64) DEFAULT '' COMMENT '审核人或提交人'," +
                    "  review_type VARCHAR(32) DEFAULT '' COMMENT '审核记录类型'," +
                    "  ai_suggestion TEXT COMMENT 'AI审核建议'," +
                    "  reject_reason VARCHAR(1000) DEFAULT '' COMMENT '拒绝原因'," +
                    "  status INT DEFAULT 0 COMMENT '状态 0未读 1已读'," +
                    "  create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'," +
                    "  INDEX idx_note_id (note_id)," +
                    "  INDEX idx_create_time (create_time)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='笔记审核记录表'"
            );
            System.out.println("[OK] t_note_review 表已就绪");

            System.out.println("=== 数据库自动迁移完成 ===");

        } catch (Exception e) {
            System.err.println("数据库迁移出错(可忽略): " + e.getMessage());
        }
    }

    private boolean columnExists(Connection conn, String table, String column) throws Exception {
        try (ResultSet rs = conn.getMetaData().getColumns(null, null, table, column)) {
            return rs.next();
        }
    }

    private void addColumnIfNotExists(Connection conn, Statement stmt, String table, String column, String alterSql) {
        try {
            if (!columnExists(conn, table, column)) {
                stmt.executeUpdate(alterSql);
                System.out.println("[OK] 已添加列: " + table + "." + column);
            } else {
                System.out.println("[SKIP] 列已存在: " + table + "." + column);
            }
        } catch (Exception e) {
            System.err.println("[WARN] 添加列 " + table + "." + column + " 时: " + e.getMessage());
        }
    }
}
