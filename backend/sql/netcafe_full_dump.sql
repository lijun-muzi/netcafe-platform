-- MySQL dump 10.13  Distrib 8.0.40, for macos14.7 (arm64)
--
-- Host: 127.0.0.1    Database: netcafe-platform
-- ------------------------------------------------------
-- Server version	8.0.40

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

--
-- Table structure for table `admin`
--

DROP TABLE IF EXISTS `admin`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `admin` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `name` varchar(50) NOT NULL,
  `role` varchar(20) NOT NULL,
  `status` tinyint NOT NULL DEFAULT '1',
  `last_login_time` datetime DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_admin_username` (`username`),
  KEY `idx_admin_status` (`status`),
  KEY `idx_admin_role_status` (`role`,`status`),
  KEY `idx_admin_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=1008 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `admin`
--

LOCK TABLES `admin` WRITE;
/*!40000 ALTER TABLE `admin` DISABLE KEYS */;
INSERT INTO `admin` VALUES (4,'superadmin_test','123456','李青啊啊','SUPER_ADMIN',1,'2026-03-25 11:47:29','2026-01-30 11:10:52','2026-03-24 22:48:22'),(5,'admin_01','123456','王杰','ADMIN',1,'2026-03-24 23:13:17','2026-01-30 11:10:52','2026-03-24 22:00:20'),(6,'admin_02','123456','陈璐','ADMIN',0,'2026-01-11 20:16:00','2026-01-30 11:10:52','2026-03-24 22:00:20'),(10,'admin_03','123456','赵敏','ADMIN',1,'2026-03-23 18:12:00','2026-03-24 22:00:25','2026-03-24 22:00:25'),(11,'admin_04','$2a$10$hWMgDqQTE9ml92dvVbW/KeS33VIbqoyjyR0c1.gD.FxOmN2QvdfTG','张凯','ADMIN',1,'2026-03-24 22:15:23','2026-03-24 22:00:25','2026-03-24 22:14:52'),(1001,'super_alpha','$2a$10$hWMgDqQTE9ml92dvVbW/KeS33VIbqoyjyR0c1.gD.FxOmN2QvdfTG','赵峰','SUPER_ADMIN',1,'2026-03-24 08:35:00','2026-02-01 09:00:00','2026-03-24 08:35:00'),(1002,'super_beta','$2a$10$hWMgDqQTE9ml92dvVbW/KeS33VIbqoyjyR0c1.gD.FxOmN2QvdfTG','马骁','SUPER_ADMIN',1,'2026-03-23 22:10:00','2026-02-03 10:00:00','2026-03-23 22:10:00'),(1003,'super_gamma','$2a$10$hWMgDqQTE9ml92dvVbW/KeS33VIbqoyjyR0c1.gD.FxOmN2QvdfTG','高宁','SUPER_ADMIN',0,'2026-03-10 18:20:00','2026-02-08 11:30:00','2026-03-10 18:20:00'),(1004,'super_delta','$2a$10$hWMgDqQTE9ml92dvVbW/KeS33VIbqoyjyR0c1.gD.FxOmN2QvdfTG','程雪','SUPER_ADMIN',0,'2026-03-08 14:15:00','2026-02-12 12:10:00','2026-03-08 14:15:00'),(1005,'admin_ops_05','$2a$10$hWMgDqQTE9ml92dvVbW/KeS33VIbqoyjyR0c1.gD.FxOmN2QvdfTG','林嘉','ADMIN',1,'2026-03-24 09:05:00','2026-02-15 09:30:00','2026-03-24 09:05:00'),(1006,'admin_ops_06','$2a$10$hWMgDqQTE9ml92dvVbW/KeS33VIbqoyjyR0c1.gD.FxOmN2QvdfTG','周策','ADMIN',0,'2026-03-11 16:20:00','2026-02-18 10:20:00','2026-03-11 16:20:00'),(1007,'admin_ops_07','$2a$10$hWMgDqQTE9ml92dvVbW/KeS33VIbqoyjyR0c1.gD.FxOmN2QvdfTG','唐雯','ADMIN',0,'2026-03-07 13:40:00','2026-02-20 11:20:00','2026-03-07 13:40:00');
/*!40000 ALTER TABLE `admin` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `audit_log`
--

DROP TABLE IF EXISTS `audit_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `audit_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `operator_id` bigint NOT NULL,
  `operator_role` varchar(20) NOT NULL,
  `action` varchar(50) NOT NULL,
  `target_type` varchar(50) NOT NULL,
  `target_id` bigint NOT NULL,
  `before_data` json DEFAULT NULL,
  `after_data` json DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_audit_created` (`created_at`),
  KEY `idx_audit_operator` (`operator_id`),
  KEY `idx_audit_target_type` (`target_type`),
  KEY `idx_audit_action_created` (`action`,`created_at`)
) ENGINE=InnoDB AUTO_INCREMENT=7053 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `audit_log`
--

LOCK TABLES `audit_log` WRITE;
/*!40000 ALTER TABLE `audit_log` DISABLE KEYS */;
INSERT INTO `audit_log` VALUES (1,4,'SUPER_ADMIN','保存基础配置','SYSTEM_CONFIG',1,'{\"targetLabel\": \"基础配置\", \"changeSummary\": \"默认单价 0.10 元/分钟，提醒阈值 60 分钟\", \"defaultPricePerMin\": 0.1, \"lowBalanceThresholdMinutes\": 60}','{\"targetLabel\": \"基础配置\", \"changeSummary\": \"默认单价 0.10 元/分钟，提醒阈值 60 分钟\", \"defaultPricePerMin\": 0.1, \"lowBalanceThresholdMinutes\": 60}','2026-03-24 23:12:48'),(2,4,'SUPER_ADMIN','新增模板','MACHINE_TEMPLATE',3,NULL,'{\"name\": \"审计联调模板\", \"configJson\": \"{\\\"cpu\\\":\\\"R5\\\",\\\"gpu\\\":\\\"RTX 4060\\\",\\\"memory\\\":\\\"16G\\\",\\\"spec\\\":\\\"R5 / RTX 4060 / 16G\\\"}\", \"targetLabel\": \"审计联调模板\", \"changeSummary\": \"R5 / RTX 4060 / 16G\"}','2026-03-24 23:13:12'),(3,5,'ADMIN','强制下机','SESSION',5,'{\"amount\": 7.0, \"status\": 0, \"endTime\": null, \"userName\": \"许晨\", \"startTime\": \"2026-03-24T13:10:00\", \"machineCode\": \"B01\", \"targetLabel\": \"用户 许晨 / 机位 B01\", \"changeSummary\": \"订单金额 7.00，状态 进行中\"}','{\"amount\": 7.0, \"status\": 2, \"endTime\": \"2026-03-24T23:13:31\", \"userName\": \"许晨\", \"startTime\": \"2026-03-24T13:10:00\", \"machineCode\": \"B01\", \"targetLabel\": \"用户 许晨 / 机位 B01\", \"changeSummary\": \"订单金额 7.00，状态 强制结束\"}','2026-03-24 23:13:31'),(4,4,'SUPER_ADMIN','编辑模板','MACHINE_TEMPLATE',3,'{\"name\": \"审计联调模板\", \"configJson\": \"{\\\"cpu\\\": \\\"R5\\\", \\\"gpu\\\": \\\"RTX 4060\\\", \\\"spec\\\": \\\"R5 / RTX 4060 / 16G\\\", \\\"memory\\\": \\\"16G\\\"}\", \"targetLabel\": \"审计联调模板\", \"changeSummary\": \"R5 / RTX 4060 / 16G\"}','{\"name\": \"审计联调模板-更新\", \"configJson\": \"{\\\"cpu\\\": \\\"R7\\\", \\\"gpu\\\": \\\"RTX 4060\\\", \\\"spec\\\": \\\"R7 / RTX 4060 / 32G\\\", \\\"memory\\\": \\\"32G\\\"}\", \"targetLabel\": \"审计联调模板-更新\", \"changeSummary\": \"R7 / RTX 4060 / 32G\"}','2026-03-24 23:13:56'),(5,4,'SUPER_ADMIN','删除模板','MACHINE_TEMPLATE',3,'{\"name\": \"审计联调模板-更新\", \"configJson\": \"{\\\"cpu\\\": \\\"R7\\\", \\\"gpu\\\": \\\"RTX 4060\\\", \\\"spec\\\": \\\"R7 / RTX 4060 / 32G\\\", \\\"memory\\\": \\\"32G\\\"}\", \"targetLabel\": \"审计联调模板-更新\", \"changeSummary\": \"R7 / RTX 4060 / 32G\"}',NULL,'2026-03-24 23:13:56'),(6,5,'ADMIN','开通上机','SESSION',9,NULL,'{\"userName\": \"张伟\", \"startTime\": \"2026-03-24T23:14:52\", \"machineCode\": \"D-101\", \"targetLabel\": \"用户 张伟 / 机位 D-101\", \"changeSummary\": \"用户 张伟 在机位 D-101 开通上机\", \"priceSnapshot\": 0.1}','2026-03-24 23:14:52'),(7,4,'SUPER_ADMIN','线下充值','USER',5,NULL,'{\"amount\": 10.0, \"remark\": \"订单页联调\", \"channel\": \"现金\", \"userName\": \"刘阳\", \"targetLabel\": \"用户 刘阳\", \"changeSummary\": \"为用户 刘阳 充值 10.00 元，渠道 现金\"}','2026-03-24 23:24:34'),(7001,4,'SUPER_ADMIN','保存基础配置','SYSTEM_CONFIG',1,'{\"targetLabel\": \"基础配置\", \"changeSummary\": \"默认单价 0.10 元/分钟，提醒阈值 60 分钟\"}','{\"targetLabel\": \"基础配置\", \"changeSummary\": \"默认单价 0.12 元/分钟，提醒阈值 50 分钟\"}','2026-03-05 09:00:00'),(7002,1001,'SUPER_ADMIN','保存基础配置','SYSTEM_CONFIG',1,'{\"targetLabel\": \"基础配置\", \"changeSummary\": \"默认单价 0.12 元/分钟，提醒阈值 50 分钟\"}','{\"targetLabel\": \"基础配置\", \"changeSummary\": \"默认单价 0.10 元/分钟，提醒阈值 45 分钟\"}','2026-03-08 10:00:00'),(7003,1002,'SUPER_ADMIN','保存基础配置','SYSTEM_CONFIG',1,'{\"targetLabel\": \"基础配置\", \"changeSummary\": \"默认单价 0.10 元/分钟，提醒阈值 45 分钟\"}','{\"targetLabel\": \"基础配置\", \"changeSummary\": \"默认单价 0.11 元/分钟，提醒阈值 40 分钟\"}','2026-03-12 11:30:00'),(7004,4,'SUPER_ADMIN','保存基础配置','SYSTEM_CONFIG',1,'{\"targetLabel\": \"基础配置\", \"changeSummary\": \"默认单价 0.11 元/分钟，提醒阈值 40 分钟\"}','{\"targetLabel\": \"基础配置\", \"changeSummary\": \"默认单价 0.09 元/分钟，提醒阈值 55 分钟\"}','2026-03-16 16:10:00'),(7005,1001,'SUPER_ADMIN','保存基础配置','SYSTEM_CONFIG',1,'{\"targetLabel\": \"基础配置\", \"changeSummary\": \"默认单价 0.09 元/分钟，提醒阈值 55 分钟\"}','{\"targetLabel\": \"基础配置\", \"changeSummary\": \"默认单价 0.10 元/分钟，提醒阈值 60 分钟\"}','2026-03-22 18:20:00'),(7006,4,'SUPER_ADMIN','新增模板','MACHINE_TEMPLATE',1,NULL,'{\"targetLabel\": \"电竞标准台\", \"changeSummary\": \"i5 / RTX 3060 / 16G / 144Hz\"}','2026-02-26 10:00:00'),(7007,1001,'SUPER_ADMIN','新增模板','MACHINE_TEMPLATE',2,NULL,'{\"targetLabel\": \"电竞旗舰台\", \"changeSummary\": \"i7 / RTX 4070 / 32G / 2K屏\"}','2026-02-27 10:30:00'),(7008,1002,'SUPER_ADMIN','新增模板','MACHINE_TEMPLATE',3001,NULL,'{\"targetLabel\": \"电竞高刷台\", \"changeSummary\": \"i7 / RTX 4070 / 32G / 240Hz\"}','2026-03-01 10:40:00'),(7009,4,'SUPER_ADMIN','新增模板','MACHINE_TEMPLATE',3002,NULL,'{\"targetLabel\": \"直播双屏台\", \"changeSummary\": \"R7 / RTX 4070 Super / 32G / 双屏\"}','2026-03-02 11:00:00'),(7010,1001,'SUPER_ADMIN','新增模板','MACHINE_TEMPLATE',3003,NULL,'{\"targetLabel\": \"训练入门台\", \"changeSummary\": \"i5 / GTX 1660 / 16G / 1080p\"}','2026-03-03 11:20:00'),(7011,4,'SUPER_ADMIN','编辑模板','MACHINE_TEMPLATE',1,'{\"targetLabel\": \"电竞标准台\", \"changeSummary\": \"i5 / RTX 3060 / 16G / 144Hz\"}','{\"targetLabel\": \"电竞标准台\", \"changeSummary\": \"i5 / RTX 3060 / 16G / 165Hz\"}','2026-03-05 13:10:00'),(7012,1001,'SUPER_ADMIN','编辑模板','MACHINE_TEMPLATE',2,'{\"targetLabel\": \"电竞旗舰台\", \"changeSummary\": \"i7 / RTX 4070 / 32G / 2K屏\"}','{\"targetLabel\": \"电竞旗舰台\", \"changeSummary\": \"i7 / RTX 4070 / 32G / 240Hz\"}','2026-03-06 14:20:00'),(7013,1002,'SUPER_ADMIN','编辑模板','MACHINE_TEMPLATE',3001,'{\"targetLabel\": \"电竞高刷台\", \"changeSummary\": \"i7 / RTX 4070 / 32G / 165Hz\"}','{\"targetLabel\": \"电竞高刷台\", \"changeSummary\": \"i7 / RTX 4070 / 32G / 240Hz\"}','2026-03-07 15:00:00'),(7014,4,'SUPER_ADMIN','编辑模板','MACHINE_TEMPLATE',3002,'{\"targetLabel\": \"直播双屏台\", \"changeSummary\": \"R7 / RTX 4070 / 32G / 单屏\"}','{\"targetLabel\": \"直播双屏台\", \"changeSummary\": \"R7 / RTX 4070 Super / 32G / 双屏\"}','2026-03-08 15:40:00'),(7015,1001,'SUPER_ADMIN','编辑模板','MACHINE_TEMPLATE',3004,'{\"targetLabel\": \"设计渲染台\", \"changeSummary\": \"R9 / RTX 4080 / 64G / 2K\"}','{\"targetLabel\": \"设计渲染台\", \"changeSummary\": \"R9 / RTX 4080 / 64G / 4K\"}','2026-03-09 16:30:00'),(7016,4,'SUPER_ADMIN','删除模板','MACHINE_TEMPLATE',3011,'{\"targetLabel\": \"入门旧模板\", \"changeSummary\": \"i3 / GTX 1050 / 8G / 1080p\"}',NULL,'2026-03-09 18:00:00'),(7017,1001,'SUPER_ADMIN','删除模板','MACHINE_TEMPLATE',3012,'{\"targetLabel\": \"包夜老模板\", \"changeSummary\": \"i5 / GTX 1060 / 16G / 1080p\"}',NULL,'2026-03-10 18:20:00'),(7018,1002,'SUPER_ADMIN','删除模板','MACHINE_TEMPLATE',3013,'{\"targetLabel\": \"赛事备用模板\", \"changeSummary\": \"i7 / RTX 3070 / 32G / 165Hz\"}',NULL,'2026-03-11 18:40:00'),(7019,4,'SUPER_ADMIN','删除模板','MACHINE_TEMPLATE',3014,'{\"targetLabel\": \"双人位旧模板\", \"changeSummary\": \"R5 / RTX 2060 / 16G / 双屏\"}',NULL,'2026-03-12 19:10:00'),(7020,1001,'SUPER_ADMIN','删除模板','MACHINE_TEMPLATE',3015,'{\"targetLabel\": \"高配试验模板\", \"changeSummary\": \"R9 / RTX 3090 / 64G / 4K\"}',NULL,'2026-03-13 19:30:00'),(7021,4,'SUPER_ADMIN','机位调价','MACHINE',1,'{\"rawValue\": \"0.10\", \"targetLabel\": \"机位 A01\", \"changeSummary\": \"0.10 → 0.12\"}','{\"rawValue\": \"0.12\", \"targetLabel\": \"机位 A01\", \"changeSummary\": \"0.10 → 0.12\"}','2026-03-05 20:00:00'),(7022,1001,'SUPER_ADMIN','机位调价','MACHINE',2,'{\"rawValue\": \"0.08\", \"targetLabel\": \"机位 A02\", \"changeSummary\": \"0.08 → 0.10\"}','{\"rawValue\": \"0.10\", \"targetLabel\": \"机位 A02\", \"changeSummary\": \"0.08 → 0.10\"}','2026-03-08 20:20:00'),(7023,1002,'SUPER_ADMIN','机位调价','MACHINE',4002,'{\"rawValue\": \"0.10\", \"targetLabel\": \"机位 F-201\", \"changeSummary\": \"0.10 → 0.12\"}','{\"rawValue\": \"0.12\", \"targetLabel\": \"机位 F-201\", \"changeSummary\": \"0.10 → 0.12\"}','2026-03-12 20:40:00'),(7024,4,'SUPER_ADMIN','机位调价','MACHINE',4004,'{\"rawValue\": \"0.12\", \"targetLabel\": \"机位 G-301\", \"changeSummary\": \"0.12 → 0.15\"}','{\"rawValue\": \"0.15\", \"targetLabel\": \"机位 G-301\", \"changeSummary\": \"0.12 → 0.15\"}','2026-03-16 21:00:00'),(7025,1001,'SUPER_ADMIN','机位调价','MACHINE',4009,'{\"rawValue\": \"0.12\", \"targetLabel\": \"机位 H-404\", \"changeSummary\": \"0.12 → 0.16\"}','{\"rawValue\": \"0.16\", \"targetLabel\": \"机位 H-404\", \"changeSummary\": \"0.12 → 0.16\"}','2026-03-22 21:20:00'),(7026,4,'SUPER_ADMIN','批量建机位','MACHINE_BATCH',1,NULL,'{\"targetLabel\": \"批量建机位\", \"changeSummary\": \"模板 电竞标准台，新增 8 台机位\"}','2026-03-06 09:30:00'),(7027,1001,'SUPER_ADMIN','批量建机位','MACHINE_BATCH',2,NULL,'{\"targetLabel\": \"批量建机位\", \"changeSummary\": \"模板 电竞旗舰台，新增 6 台机位\"}','2026-03-09 09:50:00'),(7028,1002,'SUPER_ADMIN','批量建机位','MACHINE_BATCH',3001,NULL,'{\"targetLabel\": \"批量建机位\", \"changeSummary\": \"模板 电竞高刷台，新增 5 台机位\"}','2026-03-13 10:10:00'),(7029,4,'SUPER_ADMIN','批量建机位','MACHINE_BATCH',3002,NULL,'{\"targetLabel\": \"批量建机位\", \"changeSummary\": \"模板 直播双屏台，新增 4 台机位\"}','2026-03-17 10:30:00'),(7030,1001,'SUPER_ADMIN','批量建机位','MACHINE_BATCH',3003,NULL,'{\"targetLabel\": \"批量建机位\", \"changeSummary\": \"模板 训练入门台，新增 7 台机位\"}','2026-03-21 10:50:00'),(7031,5,'ADMIN','开通上机','SESSION',1,NULL,'{\"targetLabel\": \"会话#1\", \"changeSummary\": \"为用户 张伟 开通机位 A01\"}','2026-03-24 08:12:00'),(7032,11,'ADMIN','开通上机','SESSION',9,NULL,'{\"targetLabel\": \"会话#9\", \"changeSummary\": \"为用户 张伟 开通机位 D-101\"}','2026-03-24 23:14:52'),(7033,1005,'ADMIN','开通上机','SESSION',5001,NULL,'{\"targetLabel\": \"会话#5001\", \"changeSummary\": \"为用户 韩松 开通机位 F-201\"}','2026-03-24 18:20:00'),(7034,1005,'ADMIN','开通上机','SESSION',5002,NULL,'{\"targetLabel\": \"会话#5002\", \"changeSummary\": \"为用户 沈琳 开通机位 F-202\"}','2026-03-24 19:05:00'),(7035,1005,'ADMIN','开通上机','SESSION',5003,NULL,'{\"targetLabel\": \"会话#5003\", \"changeSummary\": \"为用户 郑凯 开通机位 G-301\"}','2026-03-24 20:15:00'),(7036,5,'ADMIN','强制下机','SESSION',3,'{\"targetLabel\": \"会话#3\", \"changeSummary\": \"用户 孙雨 正在机位 A01 上机\"}','{\"targetLabel\": \"会话#3\", \"changeSummary\": \"用户 孙雨 强制下机，扣费 3.00 元\"}','2026-03-20 18:30:00'),(7037,11,'ADMIN','强制下机','SESSION',5,'{\"targetLabel\": \"会话#5\", \"changeSummary\": \"用户 许晨 正在机位 B01 上机\"}','{\"targetLabel\": \"会话#5\", \"changeSummary\": \"用户 许晨 强制下机，扣费 7.00 元\"}','2026-03-24 23:13:31'),(7038,4,'SUPER_ADMIN','强制下机','SESSION',8,'{\"targetLabel\": \"会话#8\", \"changeSummary\": \"用户 周婷 正在机位 A01 上机\"}','{\"targetLabel\": \"会话#8\", \"changeSummary\": \"用户 周婷 强制下机，扣费 0.12 元\"}','2026-03-24 22:21:46'),(7039,1005,'ADMIN','强制下机','SESSION',5008,'{\"targetLabel\": \"会话#5008\", \"changeSummary\": \"用户 郑凯 正在机位 A02 上机\"}','{\"targetLabel\": \"会话#5008\", \"changeSummary\": \"用户 郑凯 强制下机，扣费 4.50 元\"}','2026-03-22 18:45:00'),(7040,11,'ADMIN','强制下机','SESSION',5009,'{\"targetLabel\": \"会话#5009\", \"changeSummary\": \"用户 何雨晴 正在机位 E-101 上机\"}','{\"targetLabel\": \"会话#5009\", \"changeSummary\": \"用户 何雨晴 强制下机，扣费 4.00 元\"}','2026-03-23 20:10:00'),(7041,5,'ADMIN','线下充值','USER',2001,NULL,'{\"targetLabel\": \"用户 韩松\", \"changeSummary\": \"为用户 韩松 充值 50.00 元，渠道 现金\"}','2026-03-05 09:15:00'),(7042,11,'ADMIN','线下充值','USER',2002,NULL,'{\"targetLabel\": \"用户 沈琳\", \"changeSummary\": \"为用户 沈琳 充值 80.00 元，渠道 现金\"}','2026-03-08 20:10:00'),(7043,1005,'ADMIN','线下充值','USER',2003,NULL,'{\"targetLabel\": \"用户 郑凯\", \"changeSummary\": \"为用户 郑凯 充值 100.00 元，渠道 支付宝\"}','2026-03-04 12:10:00'),(7044,4,'SUPER_ADMIN','线下充值','USER',2004,NULL,'{\"targetLabel\": \"用户 何雨晴\", \"changeSummary\": \"为用户 何雨晴 充值 40.00 元，渠道 其他\"}','2026-03-24 11:30:00'),(7045,10,'ADMIN','线下充值','USER',2006,NULL,'{\"targetLabel\": \"用户 许诺\", \"changeSummary\": \"为用户 许诺 充值 45.00 元，渠道 支付宝\"}','2026-03-17 15:05:00'),(7046,4,'SUPER_ADMIN','保存基础配置','SYSTEM_CONFIG',1,'{\"targetLabel\": \"基础配置\", \"changeSummary\": \"默认单价 0.10 元/分钟，提醒阈值 60 分钟\", \"defaultPricePerMin\": 0.1, \"lowBalanceThresholdMinutes\": 60}','{\"targetLabel\": \"基础配置\", \"changeSummary\": \"默认单价 0.10 元/分钟，提醒阈值 60 分钟\", \"defaultPricePerMin\": 0.1, \"lowBalanceThresholdMinutes\": 60}','2026-03-25 01:16:06'),(7047,4,'SUPER_ADMIN','保存基础配置','SYSTEM_CONFIG',1,'{\"targetLabel\": \"基础配置\", \"changeSummary\": \"默认单价 0.10 元/分钟，提醒阈值 60 分钟\", \"defaultPricePerMin\": 0.1, \"lowBalanceThresholdMinutes\": 60}','{\"targetLabel\": \"基础配置\", \"changeSummary\": \"默认单价 1.00 元/分钟，提醒阈值 60 分钟\", \"defaultPricePerMin\": 1, \"lowBalanceThresholdMinutes\": 60}','2026-03-25 01:16:14'),(7048,4,'SUPER_ADMIN','保存基础配置','SYSTEM_CONFIG',1,'{\"targetLabel\": \"基础配置\", \"changeSummary\": \"默认单价 1.00 元/分钟，提醒阈值 60 分钟\", \"defaultPricePerMin\": 1, \"lowBalanceThresholdMinutes\": 60}','{\"targetLabel\": \"基础配置\", \"changeSummary\": \"默认单价 1.00 元/分钟，提醒阈值 60 分钟\", \"defaultPricePerMin\": 1, \"lowBalanceThresholdMinutes\": 60}','2026-03-25 01:16:30'),(7049,4,'SUPER_ADMIN','保存基础配置','SYSTEM_CONFIG',1,'{\"targetLabel\": \"基础配置\", \"changeSummary\": \"默认单价 1.00 元/分钟，提醒阈值 60 分钟\", \"defaultPricePerMin\": 1, \"lowBalanceThresholdMinutes\": 60}','{\"targetLabel\": \"基础配置\", \"changeSummary\": \"默认单价 1.00 元/分钟，提醒阈值 60 分钟\", \"defaultPricePerMin\": 1, \"lowBalanceThresholdMinutes\": 60}','2026-03-25 01:16:37'),(7050,4,'SUPER_ADMIN','保存基础配置','SYSTEM_CONFIG',1,'{\"targetLabel\": \"基础配置\", \"changeSummary\": \"默认单价 1.00 元/分钟，提醒阈值 60 分钟\", \"defaultPricePerMin\": 1, \"lowBalanceThresholdMinutes\": 60}','{\"targetLabel\": \"基础配置\", \"changeSummary\": \"默认单价 2.00 元/分钟，提醒阈值 60 分钟\", \"defaultPricePerMin\": 2, \"lowBalanceThresholdMinutes\": 60}','2026-03-25 01:16:44'),(7051,4,'SUPER_ADMIN','保存基础配置','SYSTEM_CONFIG',1,'{\"targetLabel\": \"基础配置\", \"changeSummary\": \"默认单价 2.00 元/分钟，提醒阈值 60 分钟\", \"defaultPricePerMin\": 2, \"lowBalanceThresholdMinutes\": 60}','{\"targetLabel\": \"基础配置\", \"changeSummary\": \"默认单价 2.00 元/分钟，提醒阈值 30 分钟\", \"defaultPricePerMin\": 2, \"lowBalanceThresholdMinutes\": 30}','2026-03-25 01:50:05'),(7052,4,'SUPER_ADMIN','强制下机','SESSION',5105,'{\"amount\": 0.36, \"status\": 3, \"endTime\": null, \"userName\": \"机位联调二\", \"startTime\": \"2026-03-25T02:05:38\", \"machineCode\": \"A-027\", \"targetLabel\": \"用户 机位联调二 / 机位 A-027\", \"changeSummary\": \"订单金额 0.36，状态 暂停中\"}','{\"amount\": 0.6, \"status\": 2, \"endTime\": \"2026-03-25T02:17:15\", \"userName\": \"机位联调二\", \"startTime\": \"2026-03-25T02:05:38\", \"machineCode\": \"A-027\", \"targetLabel\": \"用户 机位联调二 / 机位 A-027\", \"changeSummary\": \"订单金额 0.60，状态 强制结束\"}','2026-03-25 02:17:15');
/*!40000 ALTER TABLE `audit_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `machine`
--

DROP TABLE IF EXISTS `machine`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `machine` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `code` varchar(32) NOT NULL,
  `status` tinyint NOT NULL DEFAULT '0',
  `price_per_min` decimal(10,4) NOT NULL DEFAULT '0.1000',
  `config_json` json NOT NULL,
  `template_id` bigint DEFAULT NULL,
  `last_maintained_at` datetime DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_machine_code` (`code`),
  KEY `idx_machine_status` (`status`),
  KEY `idx_machine_price` (`price_per_min`),
  KEY `idx_machine_template` (`template_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4106 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `machine`
--

LOCK TABLES `machine` WRITE;
/*!40000 ALTER TABLE `machine` DISABLE KEYS */;
INSERT INTO `machine` VALUES (1,'A01',0,0.1200,'{\"spec\": \"i5/16G/RTX3060\", \"zone\": \"A区\"}',NULL,'2026-03-18 09:00:00','2026-03-24 22:00:51','2026-03-24 22:21:46'),(2,'A02',0,0.1000,'{\"spec\": \"i5/16G/RTX4060\", \"zone\": \"A区\"}',NULL,'2026-03-18 09:10:00','2026-03-24 22:00:51','2026-03-24 22:00:51'),(3,'B01',0,0.1000,'{\"spec\": \"R7/32G/RTX4070\", \"zone\": \"B区\"}',NULL,'2026-03-18 09:20:00','2026-03-24 22:00:51','2026-03-24 23:13:31'),(4,'C01',2,0.1200,'{\"spec\": \"i7/32G/RTX4080\", \"zone\": \"电竞区\"}',NULL,'2026-03-17 21:00:00','2026-03-24 22:00:51','2026-03-24 22:00:51'),(5,'D-101',1,0.1000,'{\"cpu\": \"i5\", \"gpu\": \"RTX 3060\", \"spec\": \"i5 / RTX 3060 / 16G\", \"memory\": \"16G\"}',1,'2026-03-24 22:21:45','2026-03-24 22:21:45','2026-03-24 23:14:52'),(6,'D-102',0,0.1000,'{\"cpu\": \"i5\", \"gpu\": \"RTX 3060\", \"spec\": \"i5 / RTX 3060 / 16G\", \"memory\": \"16G\"}',1,'2026-03-24 22:21:45','2026-03-24 22:21:45','2026-03-24 22:21:45'),(4001,'E-101',0,0.1000,'{\"cpu\": \"i5\", \"gpu\": \"GTX 1660\", \"spec\": \"i5 / GTX 1660 / 16G / 1080p\", \"memory\": \"16G\", \"display\": \"1080p\"}',3003,'2026-03-22 10:00:00','2026-03-01 09:00:00','2026-03-22 10:00:00'),(4002,'F-201',1,0.1200,'{\"cpu\": \"i7\", \"gpu\": \"RTX 4070\", \"spec\": \"i7 / RTX 4070 / 32G / 240Hz\", \"memory\": \"32G\", \"display\": \"240Hz\"}',3001,'2026-03-20 14:30:00','2026-03-02 09:10:00','2026-03-24 18:20:00'),(4003,'F-202',1,0.1200,'{\"cpu\": \"i7\", \"gpu\": \"RTX 4070\", \"spec\": \"i7 / RTX 4070 / 32G / 240Hz\", \"memory\": \"32G\", \"display\": \"240Hz\"}',3001,'2026-03-20 14:35:00','2026-03-02 09:20:00','2026-03-24 19:05:00'),(4004,'G-301',0,0.1500,'{\"cpu\": \"R7\", \"gpu\": \"RTX 4070 Super\", \"spec\": \"R7 / RTX 4070 Super / 32G / 双屏\", \"memory\": \"32G\", \"display\": \"双屏\"}',3002,'2026-03-19 16:00:00','2026-03-03 09:30:00','2026-03-25 06:10:00'),(4005,'G-302',0,0.0800,'{\"cpu\": \"i5\", \"gpu\": \"GTX 1660\", \"spec\": \"i5 / GTX 1660 / 16G / 1080p\", \"memory\": \"16G\", \"display\": \"1080p\"}',3003,'2026-03-21 13:10:00','2026-03-03 09:40:00','2026-03-25 07:13:00'),(4006,'H-401',2,0.1000,'{\"cpu\": \"i5\", \"gpu\": \"GTX 1660\", \"spec\": \"i5 / GTX 1660 / 16G / 1080p\", \"memory\": \"16G\", \"display\": \"1080p\"}',3003,'2026-03-17 18:00:00','2026-03-04 10:00:00','2026-03-17 18:00:00'),(4007,'H-402',2,0.1200,'{\"cpu\": \"i7\", \"gpu\": \"RTX 4070\", \"spec\": \"i7 / RTX 4070 / 32G / 240Hz\", \"memory\": \"32G\", \"display\": \"240Hz\"}',3001,'2026-03-16 19:10:00','2026-03-04 10:20:00','2026-03-16 19:10:00'),(4008,'H-403',2,0.0900,'{\"cpu\": \"i5\", \"gpu\": \"GTX 1660\", \"spec\": \"i5 / GTX 1660 / 16G / 1080p\", \"memory\": \"16G\", \"display\": \"1080p\"}',3003,'2026-03-15 20:20:00','2026-03-04 10:40:00','2026-03-15 20:20:00'),(4009,'H-404',2,0.1600,'{\"cpu\": \"R9\", \"gpu\": \"RTX 4080\", \"spec\": \"R9 / RTX 4080 / 64G / 4K\", \"memory\": \"64G\", \"display\": \"4K\"}',3004,'2026-03-14 21:30:00','2026-03-04 11:00:00','2026-03-14 21:30:00'),(4101,'A-021',1,0.1200,'{\"cpu\": \"i7\", \"gpu\": \"RTX 4070\", \"memory\": \"32G\", \"display\": \"2K屏\"}',NULL,'2026-03-20 00:56:01','2026-03-25 00:56:01','2026-03-25 00:56:01'),(4102,'A-022',0,0.1000,'{\"cpu\": \"i5\", \"gpu\": \"RTX 3060\", \"memory\": \"16G\", \"display\": \"144Hz\"}',NULL,'2026-03-17 00:56:01','2026-03-25 00:56:01','2026-03-25 00:56:01'),(4103,'C-003',2,0.0800,'{\"cpu\": \"i5\", \"gpu\": \"GTX 1660\", \"memory\": \"16G\", \"display\": \"1080p\"}',NULL,'2026-03-13 00:56:01','2026-03-25 00:56:01','2026-03-25 00:56:01'),(4104,'A-027',0,0.1200,'{\"cpu\": \"i7\", \"gpu\": \"RTX 4070\", \"memory\": \"32G\", \"display\": \"2K屏\"}',NULL,'2026-03-23 00:56:01','2026-03-25 00:56:01','2026-03-25 02:17:15'),(4105,'A-011',0,0.1200,'{\"cpu\": \"i7\", \"gpu\": \"RTX 3070\", \"memory\": \"32G\", \"display\": \"240Hz\"}',NULL,'2026-03-22 00:56:01','2026-03-25 00:56:01','2026-03-25 11:51:53');
/*!40000 ALTER TABLE `machine` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `machine_template`
--

DROP TABLE IF EXISTS `machine_template`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `machine_template` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  `config_json` json NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_machine_template_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=3005 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `machine_template`
--

LOCK TABLES `machine_template` WRITE;
/*!40000 ALTER TABLE `machine_template` DISABLE KEYS */;
INSERT INTO `machine_template` VALUES (1,'电竞标准台','{\"cpu\": \"i5\", \"gpu\": \"RTX 3060\", \"spec\": \"i5 / RTX 3060 / 16G\", \"memory\": \"16G\"}','2026-03-24 22:20:20','2026-03-24 22:20:20'),(2,'电竞旗舰台','{\"cpu\": \"i7\", \"gpu\": \"RTX 4070\", \"spec\": \"i7 / RTX 4070 / 32G\", \"memory\": \"32G\"}','2026-03-24 22:20:20','2026-03-24 22:20:20'),(3001,'电竞高刷台','{\"cpu\": \"i7\", \"gpu\": \"RTX 4070\", \"spec\": \"i7 / RTX 4070 / 32G / 240Hz\", \"memory\": \"32G\", \"display\": \"240Hz\"}','2026-03-01 10:00:00','2026-03-20 18:30:00'),(3002,'直播双屏台','{\"cpu\": \"R7\", \"gpu\": \"RTX 4070 Super\", \"spec\": \"R7 / RTX 4070 Super / 32G / 双屏\", \"memory\": \"32G\", \"display\": \"双屏\"}','2026-03-02 10:30:00','2026-03-21 17:40:00'),(3003,'训练入门台','{\"cpu\": \"i5\", \"gpu\": \"GTX 1660\", \"spec\": \"i5 / GTX 1660 / 16G / 1080p\", \"memory\": \"16G\", \"display\": \"1080p\"}','2026-03-03 11:20:00','2026-03-18 15:20:00'),(3004,'设计渲染台','{\"cpu\": \"R9\", \"gpu\": \"RTX 4080\", \"spec\": \"R9 / RTX 4080 / 64G / 4K\", \"memory\": \"64G\", \"display\": \"4K\"}','2026-03-04 09:50:00','2026-03-22 16:10:00');
/*!40000 ALTER TABLE `machine_template` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `price_change_log`
--

DROP TABLE IF EXISTS `price_change_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `price_change_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `machine_id` bigint NOT NULL,
  `old_price` decimal(10,4) NOT NULL,
  `new_price` decimal(10,4) NOT NULL,
  `operator_admin_id` bigint NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_price_change_machine_created` (`machine_id`,`created_at`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `price_change_log`
--

LOCK TABLES `price_change_log` WRITE;
/*!40000 ALTER TABLE `price_change_log` DISABLE KEYS */;
INSERT INTO `price_change_log` VALUES (1,1,0.1000,0.1200,4,'2026-03-24 22:21:45');
/*!40000 ALTER TABLE `price_change_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `recharge_record`
--

DROP TABLE IF EXISTS `recharge_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `recharge_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `amount` decimal(10,2) NOT NULL,
  `channel` varchar(20) NOT NULL,
  `operator_admin_id` bigint NOT NULL,
  `remark` varchar(255) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_recharge_user_created` (`user_id`,`created_at`),
  KEY `idx_recharge_created` (`created_at`),
  KEY `idx_recharge_channel_created` (`channel`,`created_at`)
) ENGINE=InnoDB AUTO_INCREMENT=6021 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `recharge_record`
--

LOCK TABLES `recharge_record` WRITE;
/*!40000 ALTER TABLE `recharge_record` DISABLE KEYS */;
INSERT INTO `recharge_record` VALUES (2,4,100.00,'微信',4,'联调样例-张伟-开卡充值','2026-03-01 10:05:00'),(3,4,30.00,'现金',5,'联调样例-张伟-续充','2026-03-20 20:10:00'),(4,5,50.00,'支付宝',5,'联调样例-刘阳-首次充值','2026-03-03 18:40:00'),(5,6,20.00,'现金',4,'联调样例-孙雨-现金充值','2026-03-06 16:25:00'),(6,8,200.00,'微信',4,'联调样例-周婷-活动充值','2026-03-10 13:15:00'),(7,10,300.00,'微信',5,'联调样例-许晨-微信充值','2026-03-24 12:10:00'),(9,5,10.00,'现金',4,'订单页联调','2026-03-24 23:24:34'),(6001,2001,50.00,'现金',5,'办卡充值','2026-03-05 09:15:00'),(6002,2002,80.00,'现金',11,'夜场加款','2026-03-08 20:10:00'),(6003,2003,30.00,'现金',1005,'前台补差','2026-03-12 14:25:00'),(6004,2004,100.00,'现金',5,'现金充值','2026-03-18 17:40:00'),(6005,2005,20.00,'现金',4,'续费','2026-03-24 09:05:00'),(6006,2001,120.00,'微信',4,'微信扫码','2026-03-06 10:00:00'),(6007,2002,66.00,'微信',5,'线上补款','2026-03-09 11:20:00'),(6008,2006,40.00,'微信',10,'解冻前充值','2026-03-15 16:00:00'),(6009,2007,88.00,'微信',1005,'微信代充','2026-03-19 19:35:00'),(6010,2008,35.00,'微信',11,'临时充值','2026-03-23 13:45:00'),(6011,2003,100.00,'支付宝',5,'支付宝付款','2026-03-04 12:10:00'),(6012,2004,58.00,'支付宝',10,'午间活动','2026-03-07 13:30:00'),(6013,2005,200.00,'支付宝',4,'会员补款','2026-03-11 18:55:00'),(6014,2006,45.00,'支付宝',1006,'支付宝现金券','2026-03-17 15:05:00'),(6015,2007,75.00,'支付宝',11,'朋友代充','2026-03-22 21:10:00'),(6016,2008,25.00,'其他',5,'活动补贴','2026-03-03 09:05:00'),(6017,2001,15.00,'其他',1005,'网吧赠送','2026-03-10 10:45:00'),(6018,2002,60.00,'其他',4,'比赛奖励','2026-03-14 22:00:00'),(6019,2003,18.00,'其他',10,'系统补发','2026-03-20 08:40:00'),(6020,2004,40.00,'其他',11,'包夜赠送','2026-03-24 11:30:00');
/*!40000 ALTER TABLE `recharge_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `session_order`
--

DROP TABLE IF EXISTS `session_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `session_order` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `machine_id` bigint NOT NULL,
  `start_time` datetime NOT NULL,
  `end_time` datetime DEFAULT NULL,
  `duration_minutes` int NOT NULL DEFAULT '0',
  `price_snapshot` decimal(10,4) NOT NULL,
  `amount` decimal(10,2) NOT NULL DEFAULT '0.00',
  `billed_minutes` int NOT NULL DEFAULT '0',
  `last_billed_time` datetime DEFAULT NULL,
  `paused_at` datetime DEFAULT NULL,
  `paused_duration_seconds` int NOT NULL DEFAULT '0',
  `status` tinyint NOT NULL DEFAULT '0',
  `force_by_admin_id` bigint DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_session_status` (`status`),
  KEY `idx_session_user_start` (`user_id`,`start_time`),
  KEY `idx_session_machine_start` (`machine_id`,`start_time`),
  KEY `idx_session_start_time` (`start_time`),
  KEY `idx_session_status_start` (`status`,`start_time`)
) ENGINE=InnoDB AUTO_INCREMENT=5107 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `session_order`
--

LOCK TABLES `session_order` WRITE;
/*!40000 ALTER TABLE `session_order` DISABLE KEYS */;
INSERT INTO `session_order` VALUES (1,4,1,'2026-03-24 08:12:00','2026-03-24 10:12:00',120,0.1000,12.00,120,'2026-03-24 10:12:00',NULL,0,1,NULL,'2026-03-24 08:12:00','2026-03-24 10:12:00'),(2,5,2,'2026-03-24 09:00:00','2026-03-24 09:45:00',45,0.1000,4.50,45,'2026-03-24 09:45:00',NULL,0,1,NULL,'2026-03-24 09:00:00','2026-03-24 09:45:00'),(3,6,1,'2026-03-20 18:00:00','2026-03-20 18:30:00',30,0.1000,3.00,30,'2026-03-20 18:30:00',NULL,0,2,4,'2026-03-20 18:00:00','2026-03-20 18:30:00'),(4,8,2,'2026-03-23 17:40:00','2026-03-23 19:15:00',95,0.1000,9.50,95,'2026-03-23 19:15:00',NULL,0,1,NULL,'2026-03-23 17:40:00','2026-03-23 19:15:00'),(5,10,3,'2026-03-24 13:10:00','2026-03-24 23:13:31',70,0.1000,7.00,70,'2026-03-24 23:13:31',NULL,0,2,5,'2026-03-24 13:10:00','2026-03-24 23:13:31'),(8,8,1,'2026-03-24 22:21:45','2026-03-24 22:21:46',1,0.1200,0.12,1,'2026-03-24 22:21:46',NULL,0,2,4,'2026-03-24 22:21:45','2026-03-24 22:21:46'),(9,4,5,'2026-03-24 23:14:52',NULL,760,0.1000,76.00,760,'2026-03-25 11:55:00',NULL,0,0,NULL,'2026-03-24 23:14:52','2026-03-25 11:55:00'),(5001,2001,4002,'2026-03-24 18:20:00',NULL,1055,0.1200,126.60,1055,'2026-03-25 11:55:00',NULL,0,0,NULL,'2026-03-24 18:20:00','2026-03-25 11:55:00'),(5002,2002,4003,'2026-03-24 19:05:00',NULL,1010,0.1200,121.20,1010,'2026-03-25 11:55:00',NULL,0,0,NULL,'2026-03-24 19:05:00','2026-03-25 11:55:00'),(5003,2003,4004,'2026-03-24 20:15:00','2026-03-25 06:10:00',595,0.1500,89.20,594,'2026-03-25 06:10:00',NULL,0,2,0,'2026-03-24 20:15:00','2026-03-25 06:10:00'),(5004,2004,4005,'2026-03-24 21:40:00','2026-03-25 07:13:00',573,0.0800,45.80,572,'2026-03-25 07:13:00',NULL,0,2,0,'2026-03-24 21:40:00','2026-03-25 07:13:00'),(5005,2005,4001,'2026-03-21 09:00:00','2026-03-21 11:20:00',140,0.1000,14.00,140,'2026-03-21 11:20:00',NULL,0,1,NULL,'2026-03-21 09:00:00','2026-03-21 11:20:00'),(5006,2001,4001,'2026-03-22 13:15:00','2026-03-22 15:05:00',110,0.1000,11.00,110,'2026-03-22 15:05:00',NULL,0,1,NULL,'2026-03-22 13:15:00','2026-03-22 15:05:00'),(5007,2002,3,'2026-03-23 10:20:00','2026-03-23 12:00:00',100,0.1000,10.00,100,'2026-03-23 12:00:00',NULL,0,1,NULL,'2026-03-23 10:20:00','2026-03-23 12:00:00'),(5008,2003,2,'2026-03-22 18:00:00','2026-03-22 18:45:00',45,0.1000,4.50,45,'2026-03-22 18:45:00',NULL,0,2,5,'2026-03-22 18:00:00','2026-03-22 18:45:00'),(5009,2004,4001,'2026-03-23 19:30:00','2026-03-23 20:10:00',40,0.1000,4.00,40,'2026-03-23 20:10:00',NULL,0,2,11,'2026-03-23 19:30:00','2026-03-23 20:10:00'),(5010,2005,3,'2026-03-24 14:10:00','2026-03-24 15:05:00',55,0.1000,5.50,55,'2026-03-24 15:05:00',NULL,0,2,1005,'2026-03-24 14:10:00','2026-03-24 15:05:00'),(5101,2005,4101,'2026-03-25 00:11:01',NULL,703,0.1200,84.36,703,'2026-03-25 11:55:00',NULL,0,0,NULL,'2026-03-25 00:11:01','2026-03-25 11:55:00'),(5102,2102,4104,'2026-03-25 00:58:18','2026-03-25 00:59:14',0,0.1200,0.00,0,'2026-03-25 00:59:14',NULL,0,1,NULL,'2026-03-25 00:58:18','2026-03-25 00:59:14'),(5103,2103,4105,'2026-03-25 01:51:17','2026-03-25 01:52:33',1,0.1200,0.12,0,'2026-03-25 01:52:33',NULL,0,1,NULL,'2026-03-25 01:51:17','2026-03-25 01:52:33'),(5104,2103,4105,'2026-03-25 01:52:51','2026-03-25 03:58:00',125,0.1200,14.88,124,'2026-03-25 03:58:00',NULL,0,2,0,'2026-03-25 01:52:51','2026-03-25 03:58:00'),(5105,2102,4104,'2026-03-25 02:05:38','2026-03-25 02:17:15',5,0.1200,0.60,3,'2026-03-25 02:17:15',NULL,144,2,4,'2026-03-25 02:05:38','2026-03-25 02:17:15'),(5106,2102,4105,'2026-03-25 11:51:42','2026-03-25 11:51:53',0,0.1200,0.00,0,'2026-03-25 11:51:53',NULL,5,1,NULL,'2026-03-25 11:51:42','2026-03-25 11:51:53');
/*!40000 ALTER TABLE `session_order` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `system_config`
--

DROP TABLE IF EXISTS `system_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `system_config` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `config_key` varchar(50) NOT NULL,
  `config_value` varchar(255) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_system_config_key` (`config_key`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `system_config`
--

LOCK TABLES `system_config` WRITE;
/*!40000 ALTER TABLE `system_config` DISABLE KEYS */;
INSERT INTO `system_config` VALUES (1,'default_price_per_min','2','默认单价（元/分钟）','2026-03-25 01:50:05'),(3,'low_balance_threshold_minutes','30','余额提醒阈值（分钟）','2026-03-25 01:50:05');
/*!40000 ALTER TABLE `system_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `mobile` varchar(20) NOT NULL,
  `id_card` varchar(20) NOT NULL,
  `name` varchar(50) NOT NULL,
  `balance` decimal(10,2) NOT NULL DEFAULT '0.00',
  `status` tinyint NOT NULL DEFAULT '1',
  `register_time` datetime NOT NULL,
  `last_login_time` datetime DEFAULT NULL,
  `last_session_time` datetime DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_mobile` (`mobile`),
  UNIQUE KEY `uk_user_id_card` (`id_card`),
  KEY `idx_user_status` (`status`),
  KEY `idx_user_name_status` (`name`,`status`),
  KEY `idx_user_status_balance` (`status`,`balance`)
) ENGINE=InnoDB AUTO_INCREMENT=2106 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (4,'13812342311','320123199601011996','张伟',10.40,1,'2026-01-29 10:00:00',NULL,'2026-03-24 23:14:52','2026-01-30 11:10:58','2026-03-25 11:55:00'),(5,'18652347820','110101200101010011','刘阳',32.10,1,'2026-02-08 09:30:00',NULL,'2026-03-24 09:45:00','2026-01-30 11:10:58','2026-03-24 23:24:34'),(6,'15577884421','330101199909092222','孙雨',3.20,0,'2026-02-16 14:20:00',NULL,'2026-03-20 18:30:00','2026-01-30 11:10:58','2026-03-24 22:00:34'),(8,'13990001004','430101199805054123','周婷',127.88,1,'2026-02-02 13:20:00',NULL,'2026-03-24 22:21:46','2026-03-24 22:00:41','2026-03-24 22:21:46'),(9,'13990001005','420101199707073217','陈涛',0.00,0,'2026-02-16 18:10:00',NULL,NULL,'2026-03-24 22:00:41','2026-03-24 22:00:41'),(10,'13990001006','510101199512126654','许晨',233.50,1,'2026-03-08 09:20:00',NULL,'2026-03-24 23:13:31','2026-03-24 22:00:41','2026-03-24 23:13:31'),(11,'13244444444','1111111111','drhl',10.00,1,'2026-03-24 22:14:14',NULL,NULL,'2026-03-24 22:14:14','2026-03-24 22:14:14'),(2001,'13988002001','320101199401011111','韩松',141.90,1,'2026-02-01 10:00:00','2026-03-24 18:18:00','2026-03-24 18:20:00','2026-02-01 10:00:00','2026-03-25 11:55:00'),(2002,'13988002002','320101199502022222','沈琳',34.80,1,'2026-02-02 10:20:00','2026-03-24 19:00:00','2026-03-24 19:05:00','2026-02-02 10:20:00','2026-03-25 11:55:00'),(2003,'13988002003','320101199603033333','郑凯',0.00,1,'2026-02-03 11:00:00','2026-03-24 20:05:00','2026-03-25 06:10:00','2026-02-03 11:00:00','2026-03-25 06:10:00'),(2004,'13988002004','320101199704044444','何雨晴',0.00,1,'2026-02-04 12:00:00','2026-03-24 21:30:00','2026-03-25 07:13:00','2026-02-04 12:00:00','2026-03-25 07:13:00'),(2005,'13990002001','320101199805055555','机位联调一',121.04,1,'2026-03-25 00:56:01',NULL,'2026-03-25 00:12:17','2026-02-05 09:40:00','2026-03-25 11:55:00'),(2006,'13988002006','320101199906066666','许诺',5.00,0,'2026-02-06 10:10:00','2026-03-17 15:05:00','2026-03-16 18:30:00','2026-02-06 10:10:00','2026-03-17 15:05:00'),(2007,'13988002007','320101200007077777','白露',0.00,0,'2026-02-07 10:50:00','2026-03-22 21:10:00','2026-03-21 22:00:00','2026-02-07 10:50:00','2026-03-22 21:10:00'),(2008,'13988002008','320101200108088888','方哲',18.00,0,'2026-02-08 11:15:00','2026-03-23 13:45:00','2026-03-20 17:20:00','2026-02-08 11:15:00','2026-03-23 13:45:00'),(2102,'13990002002','320101199605066666','机位联调二',67.90,1,'2026-03-25 00:56:01','2026-03-25 11:51:50','2026-03-25 11:51:53','2026-03-25 00:56:01','2026-03-25 11:51:53'),(2103,'13990002003','320101199405077777','机位联调三',0.00,1,'2026-03-25 00:56:01','2026-03-25 01:52:51','2026-03-25 03:58:00','2026-03-25 00:56:01','2026-03-25 03:58:00'),(2104,'13990002004','320101199205088888','机位联调四',0.08,1,'2026-03-25 00:56:01',NULL,NULL,'2026-03-25 00:56:01','2026-03-25 00:56:01'),(2105,'13990002005','320101199105099999','机位冻结样例',88.00,0,'2026-03-25 00:56:01',NULL,NULL,'2026-03-25 00:56:01','2026-03-25 00:56:01');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping events for database 'netcafe-platform'
--

--
-- Dumping routines for database 'netcafe-platform'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-03-25 11:56:56
