/*
SQLyog Ultimate v12.09 (64 bit)
MySQL - 8.0.25 : Database - hivenote
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`hivenote` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `hivenote`;

/*Table structure for table `a_user` */

DROP TABLE IF EXISTS `a_user`;

CREATE TABLE `a_user` (
  `id` varchar(255) NOT NULL,
  `username` varchar(255) DEFAULT NULL COMMENT '账号',
  `password` varchar(255) DEFAULT NULL COMMENT '加密密码',
  `nickname` varchar(20) DEFAULT NULL COMMENT '昵称',
  `mobile` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像',
  `description` varchar(255) DEFAULT NULL,
  `department_id` varchar(255) DEFAULT NULL COMMENT '部门ID',
  `department_title` varchar(255) DEFAULT NULL,
  `sex` varchar(255) DEFAULT NULL,
  `type` int DEFAULT '0' COMMENT '用户类型 0普通 1管理员',
  `status` int DEFAULT '0' COMMENT '状态 0正常 -1禁用',
  `school` varchar(255) DEFAULT NULL COMMENT '学校',
  `show_email` int DEFAULT '1' COMMENT '是否公开邮箱 0不公开 1公开',
  `show_student_id` int DEFAULT '1' COMMENT '是否公开学号 0不公开 1公开',
  `del_flag` int DEFAULT '0',
  `pass_strength` varchar(2) DEFAULT NULL,
  `create_by` varchar(255) DEFAULT NULL,
  `create_time` datetime(6) DEFAULT NULL,
  `update_by` varchar(255) DEFAULT NULL,
  `update_time` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

/*Data for the table `a_user` */

insert  into `a_user`(`id`,`username`,`password`,`nickname`,`mobile`,`email`,`address`,`avatar`,`description`,`department_id`,`department_title`,`sex`,`type`,`status`,`school`,`show_email`,`show_student_id`,`del_flag`,`pass_strength`,`create_by`,`create_time`,`update_by`,`update_time`) values ('1464764315201572864','admin','e10adc3949ba59abbe56e057f20f883e','系统管理员','17857054388','admin@hivenote.com',NULL,'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',NULL,NULL,NULL,NULL,1,1,NULL,1,1,0,NULL,NULL,'2022-03-20 09:46:20.000000',NULL,NULL),('1998599353370877952','user','e10adc3949ba59abbe56e057f20f883e','用户1',NULL,'774124559@qq.com',NULL,'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',NULL,NULL,NULL,NULL,0,1,NULL,1,1,0,NULL,NULL,NULL,NULL,NULL),('1998679341432524800','user1','e10adc3949ba59abbe56e057f20f883e','user1',NULL,'774124559@qq.com',NULL,'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',NULL,NULL,NULL,NULL,0,1,NULL,1,1,0,NULL,NULL,NULL,NULL,NULL);

/*Table structure for table `a_user_role` */

DROP TABLE IF EXISTS `a_user_role`;

CREATE TABLE `a_user_role` (
  `id` varchar(255) NOT NULL,
  `user_id` varchar(255) DEFAULT NULL,
  `role_id` varchar(255) DEFAULT NULL,
  `del_flag` int DEFAULT '0',
  `create_by` varchar(255) DEFAULT NULL,
  `create_time` datetime(6) DEFAULT NULL,
  `update_by` varchar(255) DEFAULT NULL,
  `update_time` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

/*Data for the table `a_user_role` */

insert  into `a_user_role`(`id`,`user_id`,`role_id`,`del_flag`,`create_by`,`create_time`,`update_by`,`update_time`) values ('1464487931975700480','1464764315201572864','1536606659751841799',0,NULL,'2022-03-20 09:46:20.000000',NULL,NULL);

/*Table structure for table `t_comment` */

DROP TABLE IF EXISTS `t_comment`;

CREATE TABLE `t_comment` (
  `id` varchar(32) NOT NULL COMMENT '评论ID',
  `note_id` varchar(32) NOT NULL COMMENT '笔记ID',
  `content` text COMMENT '评论内容',
  `username` varchar(255) DEFAULT NULL COMMENT '评论人用户名',
  `nickname` varchar(255) DEFAULT NULL COMMENT '评论人昵称',
  `create_time` datetime DEFAULT NULL COMMENT '评论时间',
  `del_flag` int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  PRIMARY KEY (`id`),
  KEY `idx_note_id` (`note_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='评论表';

/*Data for the table `t_comment` */

insert  into `t_comment`(`id`,`note_id`,`content`,`username`,`nickname`,`create_time`,`del_flag`) values ('1998670670396862464','1998666886870405120','你好','user','用户','2025-12-10 16:26:36',0),('1998670695277473792','1998666886870405120','哈哈哈','user','用户','2025-12-10 16:26:42',0),('1998678874224807936','1','你好','user','用户','2025-12-10 16:59:12',0),('1998679133386657792','1998679106706690048','你好','user','用户','2025-12-10 17:00:14',1),('1998679153783558144','1998679106706690048','哈哈哈','user','用户','2025-12-10 17:00:19',0),('1998679404657463296','1998679106706690048','xxx','user1','user1','2025-12-10 17:01:18',0);

/*Table structure for table `t_note` */

DROP TABLE IF EXISTS `t_note`;

CREATE TABLE `t_note` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `title` varchar(100) DEFAULT NULL COMMENT '笔记标题',
  `category` varchar(32) DEFAULT NULL COMMENT '分类(CS/SE/Math/GE)',
  `summary` varchar(500) DEFAULT NULL COMMENT '简介',
  `file_url` varchar(500) DEFAULT NULL COMMENT '文件下载地址',
  `view_count` int DEFAULT '0' COMMENT '浏览量',
  `download_count` int DEFAULT '0' COMMENT '下载量',
  `status` int DEFAULT '1' COMMENT '状态 0审核中 1正常',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人(存储用户名)',
  `school` varchar(255) DEFAULT NULL COMMENT '上传者所属学校',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='数字笔记表';

/*Data for the table `t_note` */

insert  into `t_note`(`id`,`title`,`category`,`summary`,`file_url`,`view_count`,`download_count`,`status`,`create_by`,`school`,`create_time`,`update_by`,`update_time`,`del_flag`) values ('1','HiveNote开发实战手册','SE','这是一份关于如何开发HiveNote的后端指南','http://localhost:8080/file/test.pdf',1030,68,1,'admin',NULL,'2025-12-10 10:04:15',NULL,NULL,0),('1998613851481767936','测试','CS','','http://localhost:8080/zwz/file/view/20251210_27e5941d2dfa4710a89ca5a18cae638c.png',13,4,1,'user',NULL,'2025-12-10 12:40:49','user','2025-12-10 16:10:55',1),('1998666886870405120','测试','CS','测试测试测试测试测试测试测试测试','http://localhost:8080/zwz/file/view/20251210_72c596c4c59d4fc6b791ccf6baaf0e2f.zip',13,1,1,'user',NULL,'2025-12-10 16:11:34',NULL,NULL,0),('1998679106706690048','测试1','Math','测试1测试1测试1测试1测试1测试1测试1测试1测试1测试1测试1测试1测试1测试1','http://localhost:8080/zwz/file/view/20251210_e3ae82f366034e549434d88efb533d97.zip',2,1,1,'user',NULL,'2025-12-10 17:00:07',NULL,NULL,0),('1998785602103013376','测试2','GE','测试2测试2测试2测试2测试2测试2测试2测试2测试2','http://localhost:8080/zwz/file/view/20251211_10f79f41fd014e969e4deadc4d23fa6f.mp4',1,0,2,'user',NULL,'2025-12-11 00:03:18','admin','2025-12-11 00:03:53',0),('1998786352925372416','测试3','GE','测试3测试3测试3测试3测试3','http://localhost:8080/zwz/file/view/20251211_f7701ad4ffa14ff498719e96b4bcc99e.mp4',0,0,1,'user',NULL,'2025-12-11 00:06:17','admin','2025-12-11 00:06:45',0),('1998786603719585792','测试4','Math','测试4测试4测试4测试4测试4','http://localhost:8080/zwz/file/view/20251211_ce78b4ae3ec64676949533cc9688b78d.mp4',0,0,0,'user',NULL,'2025-12-11 00:07:17',NULL,NULL,0);

/*Table structure for table `t_favorite` */

DROP TABLE IF EXISTS `t_favorite`;

CREATE TABLE `t_favorite` (
  `id` varchar(32) NOT NULL COMMENT '收藏ID',
  `note_id` varchar(32) NOT NULL COMMENT '笔记ID',
  `username` varchar(255) NOT NULL COMMENT '用户名',
  `create_time` datetime DEFAULT NULL COMMENT '收藏时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_note` (`username`, `note_id`),
  KEY `idx_username` (`username`),
  KEY `idx_note_id` (`note_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='收藏表';

/*Table structure for table `t_like` */

DROP TABLE IF EXISTS `t_like`;

CREATE TABLE `t_like` (
  `id` varchar(32) NOT NULL COMMENT '点赞ID',
  `note_id` varchar(32) NOT NULL COMMENT '笔记ID',
  `username` varchar(255) NOT NULL COMMENT '用户名',
  `create_time` datetime DEFAULT NULL COMMENT '点赞时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_note` (`username`, `note_id`),
  KEY `idx_username` (`username`),
  KEY `idx_note_id` (`note_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='点赞表';

/* Add like_count and favorite_count to t_note */
ALTER TABLE `t_note` ADD COLUMN `like_count` int DEFAULT 0 COMMENT '点赞数' AFTER `download_count`;
ALTER TABLE `t_note` ADD COLUMN `favorite_count` int DEFAULT 0 COMMENT '收藏数' AFTER `like_count`;

/* Add parent_id to t_comment for nested replies */
ALTER TABLE `t_comment` ADD COLUMN `parent_id` varchar(32) DEFAULT NULL COMMENT '父评论ID(回复功能)' AFTER `note_id`;
ALTER TABLE `t_comment` ADD COLUMN `reply_to` varchar(255) DEFAULT NULL COMMENT '回复目标用户名' AFTER `parent_id`;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
