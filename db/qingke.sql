-- 青稞绿植养护系统 建表脚本（仅表结构，不含数据）
-- 用法：mysql -u root -p qingke < db/qingke.sql


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
DROP TABLE IF EXISTS `community_comment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `community_comment` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `post_id` bigint NOT NULL COMMENT '关联帖子ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `avatar_url` longtext COMMENT '头像',
  `nickname` varchar(200) DEFAULT NULL COMMENT '用户名',
  `content` longtext NOT NULL COMMENT '评论内容',
  `reply` longtext COMMENT '回复内容',
  `like_count` int DEFAULT '0' COMMENT '点赞数',
  `dislike_count` int DEFAULT '0' COMMENT '点踩数',
  `is_top` int DEFAULT '0' COMMENT '是否置顶',
  `like_user_ids` longtext COMMENT '点赞用户ID列表',
  `dislike_user_ids` longtext COMMENT '点踩用户ID列表',
  `parent_id` bigint DEFAULT NULL COMMENT '父评论ID，NULL表示主评论',
  `reply_to_user_id` bigint DEFAULT NULL COMMENT '回复的目标用户ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='社区互动评论表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `community_post`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `community_post` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `title` varchar(200) NOT NULL COMMENT '标题',
  `img_url` longtext COMMENT '图片',
  `user_id` bigint DEFAULT NULL,
  `user_account` varchar(200) DEFAULT NULL COMMENT '账号',
  `content` longtext NOT NULL COMMENT '内容',
  `publish_time` datetime DEFAULT NULL COMMENT '发布时间',
  `audit_status` tinyint NOT NULL DEFAULT '0' COMMENT '审核状态',
  `audit_reply` longtext COMMENT '审核回复',
  `like_count` int DEFAULT '0' COMMENT '点赞数',
  `dislike_count` int DEFAULT '0' COMMENT '点踩数',
  `comment_count` int DEFAULT '0' COMMENT '评论数',
  `avatar_url` varchar(255) DEFAULT NULL COMMENT '头像',
  `collect_count` int DEFAULT '0' COMMENT '收藏数',
  PRIMARY KEY (`id`),
  KEY `sfsh` (`audit_status`),
  KEY `fabushijian` (`publish_time`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='社区互动表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `operation_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `operation_log` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) NOT NULL COMMENT '操作用户',
  `role` varchar(20) DEFAULT NULL COMMENT '用户角色',
  `operation_type` varchar(20) NOT NULL COMMENT '操作类型',
  `module` varchar(50) NOT NULL COMMENT '操作模块',
  `description` varchar(500) DEFAULT NULL COMMENT '操作描述',
  `ip_address` varchar(50) DEFAULT NULL COMMENT 'IP地址',
  `operation_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `result` varchar(20) DEFAULT 'success' COMMENT '操作结果',
  `error_message` text COMMENT '错误详情',
  PRIMARY KEY (`id`),
  KEY `idx_username` (`username`),
  KEY `idx_operation_time` (`operation_time`)
) ENGINE=InnoDB AUTO_INCREMENT=11840 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='操作日志表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `plant_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `plant_category` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `category_name` varchar(200) NOT NULL COMMENT '绿植种类名称',
  PRIMARY KEY (`id`),
  UNIQUE KEY `lvzhizhonglei` (`category_name`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='绿植种类表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `plant_chat`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `plant_chat` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `user_id` bigint DEFAULT NULL COMMENT '用户ID',
  `admin_id` bigint DEFAULT NULL COMMENT '管理员ID',
  `ask` varchar(2000) DEFAULT NULL COMMENT '用户提问',
  `reply` text COMMENT 'AI回复',
  `is_replied` int DEFAULT NULL COMMENT '是否已回复',
  `is_read` int DEFAULT NULL COMMENT '是否已读',
  `avatar_url` varchar(500) DEFAULT NULL COMMENT '用户头像',
  `user_name` varchar(100) DEFAULT NULL COMMENT '用户名',
  `msg_type` varchar(50) DEFAULT NULL COMMENT '类型',
  `image_url` mediumtext COMMENT '图片URL（base64或图片路径，多模态对话时使用）',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=42 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='聊天智能助手表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `plant_knowledge`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `plant_knowledge` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `plant_name` varchar(200) NOT NULL COMMENT '植物名称',
  `category_name` varchar(200) NOT NULL COMMENT '绿植分类',
  `img_url` longtext COMMENT '配图地址',
  `video_url` longtext COMMENT '视频链接',
  `maintain_tutorial` longtext NOT NULL COMMENT '养护教程',
  `publish_date` date DEFAULT NULL COMMENT '发布日期',
  `last_click_time` datetime DEFAULT NULL COMMENT '最近点击时间',
  `click_num` int DEFAULT '0' COMMENT '点击次数',
  `collect_num` int DEFAULT '0' COMMENT '收藏数量',
  PRIMARY KEY (`id`),
  KEY `lvzhizhonglei` (`category_name`),
  KEY `idx_yanghuzhishi_lvzhizhonglei` (`category_name`)
) ENGINE=InnoDB AUTO_INCREMENT=58 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='养护知识表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `plant_maintain_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `plant_maintain_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `plant_name` varchar(200) DEFAULT NULL COMMENT '绿植名称',
  `category_name` varchar(200) DEFAULT NULL COMMENT '绿植分类',
  `img_url` varchar(500) DEFAULT NULL COMMENT '绿植图片',
  `user_account` varchar(100) DEFAULT NULL COMMENT '用户账号',
  `grow_status` varchar(100) DEFAULT NULL COMMENT '生长状态',
  `maintain_content` text COMMENT '养护记录',
  `maintain_date` varchar(50) DEFAULT NULL COMMENT '养护日期',
  `maintain_cycle` varchar(50) DEFAULT NULL COMMENT '养护周期',
  `next_maintain_time` datetime DEFAULT NULL COMMENT '下次养护时间',
  `user_id` bigint DEFAULT NULL COMMENT '用户ID',
  PRIMARY KEY (`id`),
  KEY `lvzhizhonglei` (`category_name`),
  KEY `yanghuriqi` (`maintain_date`),
  KEY `idx_yanghujilu_xiaciyanghushijian` (`next_maintain_time`)
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='养护记录表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `plant_recognize`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `plant_recognize` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `plant_name` varchar(200) DEFAULT NULL COMMENT '绿植名称',
  `img_url` longtext COMMENT '识别图片',
  `detail_link` varchar(500) DEFAULT NULL COMMENT '详情外链',
  `plant_intro` longtext COMMENT '植物介绍',
  `disease_name` varchar(200) DEFAULT NULL COMMENT '病害名称',
  `disease_desc` text COMMENT '病害描述',
  `treat_suggest` text COMMENT '治疗建议',
  `user_id` bigint DEFAULT NULL COMMENT '用户ID',
  PRIMARY KEY (`id`),
  KEY `userid` (`user_id`),
  KEY `idx_lvzhishibie_userid` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=121 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='绿植识别表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `sys_email_verify_code`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_email_verify_code` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `email` varchar(200) NOT NULL COMMENT '邮箱',
  `user_role` varchar(50) DEFAULT NULL COMMENT '角色',
  `verify_code` varchar(50) DEFAULT NULL COMMENT '验证码',
  PRIMARY KEY (`id`),
  KEY `email_role` (`email`,`user_role`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='邮箱验证码表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `sys_notice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_notice` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `title` varchar(200) NOT NULL COMMENT '标题',
  `type` varchar(200) DEFAULT '个人' COMMENT '类型',
  `brief` longtext COMMENT '简介',
  `content` longtext NOT NULL COMMENT '内容',
  `remind_time` datetime DEFAULT NULL COMMENT '提醒时间',
  `is_read` tinyint DEFAULT '0' COMMENT '是否已读：0-未读，1-已读',
  `is_sent` tinyint DEFAULT '0' COMMENT '是否已发送：0-未发送，1-已发送',
  PRIMARY KEY (`id`),
  KEY `userid` (`user_id`),
  KEY `remindtime` (`remind_time`),
  KEY `idx_userid_read` (`user_id`,`is_read`),
  KEY `idx_remindtime` (`remind_time`),
  KEY `idx_issent` (`is_sent`)
) ENGINE=InnoDB AUTO_INCREMENT=49 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='弹窗提醒表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `sys_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `zh` varchar(200) NOT NULL COMMENT '账号',
  `password` varchar(200) NOT NULL COMMENT '密码',
  `name` varchar(200) NOT NULL COMMENT '昵称',
  `gender` varchar(200) DEFAULT NULL COMMENT '性别',
  `age` int DEFAULT NULL COMMENT '年龄',
  `phone` varchar(200) DEFAULT NULL COMMENT '联系方式',
  `avatar_url` longtext COMMENT '头像',
  `register_time` date DEFAULT NULL COMMENT '注册时间',
  `email` varchar(200) DEFAULT NULL COMMENT '邮箱',
  `security_question` varchar(200) DEFAULT NULL COMMENT '密保问题',
  `security_answer` varchar(200) DEFAULT NULL COMMENT '密保答案',
  `status` int DEFAULT '0' COMMENT '状态',
  `role` varchar(20) DEFAULT 'user' COMMENT '角色：admin=超级管理员，user=APP普通用户',
  `password_wrong_num` int DEFAULT '0' COMMENT '密码错误次数',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `points` int DEFAULT '0' COMMENT '积分',
  `ai_count` int DEFAULT '0' COMMENT 'AI识别剩余次数',
  `newbie_reward_received` tinyint(1) DEFAULT '0' COMMENT '新人专享是否已领取：0=未领取，1=已领取',
  `profile_visibility` varchar(20) DEFAULT 'public' COMMENT '个人资料可见性: public-所有人, friends-仅好友, private-仅自己',
  `care_records_public` tinyint(1) DEFAULT '1' COMMENT '养护记录是否公开: 0-否, 1-是',
  `favorites_public` tinyint(1) DEFAULT '1' COMMENT '收藏内容是否公开: 0-否, 1-是',
  `signature` varchar(100) DEFAULT NULL COMMENT '个性签名',
  PRIMARY KEY (`id`),
  UNIQUE KEY `zh` (`zh`),
  KEY `idx_users_role` (`role`),
  KEY `idx_role` (`role`),
  KEY `idx_newbie_reward` (`newbie_reward_received`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `system_error`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `system_error` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `module` varchar(50) DEFAULT NULL COMMENT '模块',
  `error_type` varchar(100) DEFAULT NULL COMMENT '异常类型',
  `error_message` text COMMENT '错误信息',
  `stack_trace` text COMMENT '堆栈',
  `request_url` varchar(500) DEFAULT NULL COMMENT '请求路径',
  `request_method` varchar(10) DEFAULT NULL COMMENT '请求方法',
  `user_id` bigint DEFAULT NULL COMMENT '用户ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '发生时间',
  PRIMARY KEY (`id`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_module` (`module`)
) ENGINE=InnoDB AUTO_INCREMENT=53 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统异常日志';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `user_collect`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_collect` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `user_id` bigint DEFAULT NULL COMMENT '用户ID',
  `target_id` bigint DEFAULT NULL COMMENT '收藏内容ID',
  `target_table` varchar(100) DEFAULT NULL COMMENT '关联表名',
  `target_name` varchar(200) DEFAULT NULL COMMENT '收藏内容名称',
  `img_url` varchar(500) DEFAULT NULL COMMENT '内容图片',
  `target_type` varchar(50) DEFAULT NULL COMMENT '内容分类',
  `rec_type` varchar(50) DEFAULT NULL COMMENT '推荐类型',
  `remark` varchar(200) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `userid` (`user_id`),
  KEY `refid_tablename` (`target_id`,`target_table`)
) ENGINE=InnoDB AUTO_INCREMENT=133 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='收藏表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `user_follow`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_follow` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `follower_id` bigint NOT NULL COMMENT '关注者ID',
  `following_id` bigint NOT NULL COMMENT '被关注者ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_follow` (`follower_id`,`following_id`),
  KEY `idx_follower` (`follower_id`),
  KEY `idx_following` (`following_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户关注表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `user_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_message` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `sender_id` bigint NOT NULL COMMENT '发送者ID',
  `receiver_id` bigint NOT NULL COMMENT '接收者ID',
  `content` text NOT NULL COMMENT '消息内容',
  `is_read` tinyint DEFAULT '0' COMMENT '是否已读：0-未读，1-已读',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  PRIMARY KEY (`id`),
  KEY `idx_receiver` (`receiver_id`,`is_read`),
  KEY `idx_sender` (`sender_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='私信表';
/*!40101 SET character_set_client = @saved_cs_client */;
DROP TABLE IF EXISTS `user_visitor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_visitor` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '被访问用户ID',
  `visitor_id` bigint NOT NULL COMMENT '访客ID',
  `visit_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '访问时间',
  `is_read` tinyint DEFAULT '0' COMMENT '是否已读：0-未读，1-已读',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`,`is_read`),
  KEY `idx_visitor` (`visitor_id`),
  KEY `idx_visit_time` (`visit_time`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='访客记录表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

