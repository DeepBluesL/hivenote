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
