CREATE DATABASE  IF NOT EXISTS `movie_pos` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `movie_pos`;
-- MySQL dump 10.13  Distrib 8.0.46, for Win64 (x86_64)
--
-- Host: localhost    Database: movie_pos
-- ------------------------------------------------------
-- Server version	8.0.46

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
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
  `admin_id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` char(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'SHA-256 雜湊',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`admin_id`),
  UNIQUE KEY `uk_admin_username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系統管理者';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `admin`
--

LOCK TABLES `admin` WRITE;
/*!40000 ALTER TABLE `admin` DISABLE KEYS */;
INSERT INTO `admin` VALUES (1,'admin','a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3','系統管理員'),(2,'manager','866485796cfa8d7c0cf7111640205b83076433547577511d81f8030ae99ecea5','經理'),(4,'engineer','a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3','系統管理員');
/*!40000 ALTER TABLE `admin` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `hall`
--

DROP TABLE IF EXISTS `hall`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `hall` (
  `hall_id` int NOT NULL AUTO_INCREMENT,
  `hall_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `row_count` int NOT NULL DEFAULT '10' COMMENT '排數(至少10排)',
  `first_row_seats` int NOT NULL DEFAULT '8' COMMENT '第一排座位數',
  PRIMARY KEY (`hall_id`),
  CONSTRAINT `ck_hall_rows` CHECK ((`row_count` >= 10))
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='影廳';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `hall`
--

LOCK TABLES `hall` WRITE;
/*!40000 ALTER TABLE `hall` DISABLE KEYS */;
INSERT INTO `hall` VALUES (1,'第1廳',10,8),(2,'第2廳',10,10),(3,'第3廳',12,6);
/*!40000 ALTER TABLE `hall` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `member`
--

DROP TABLE IF EXISTS `member`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `member` (
  `member_id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '登入帳號(不可重複)',
  `password` char(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'SHA-256 雜湊',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`member_id`),
  UNIQUE KEY `uk_member_username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='會員';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `member`
--

LOCK TABLES `member` WRITE;
/*!40000 ALTER TABLE `member` DISABLE KEYS */;
INSERT INTO `member` VALUES (1,'user01','03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4','王小明','0912345678','user01@example.com','2026-09-28 19:04:36'),(2,'user02','a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3','林敏綺','0988777666','user02@example.com','2026-09-28 19:04:36'),(3,'kiki','888da5db853449fff82b07cbdbf7c755ece0783aa670bb36cc5c4cc9a68fb864','rainbow','0866555111','kiki@gmail.com','2026-09-28 19:32:48');
/*!40000 ALTER TABLE `member` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `movie`
--

DROP TABLE IF EXISTS `movie`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `movie` (
  `movie_id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '電影名稱',
  `duration_min` int NOT NULL DEFAULT '120' COMMENT '片長(分鐘)',
  `rating` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '普遍級' COMMENT '分級',
  `synopsis` text COLLATE utf8mb4_unicode_ci COMMENT '電影簡介',
  `director` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '導演',
  `actors` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '主要演員，以逗號分隔',
  `genre` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '類型：動作、喜劇、動畫…',
  `release_date` date NOT NULL COMMENT '上映日期',
  `trailer_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '預告片網址',
  `is_active` tinyint(1) NOT NULL DEFAULT '1' COMMENT '1=前台顯示，0=下架隱藏',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最後修改時間',
  PRIMARY KEY (`movie_id`),
  UNIQUE KEY `uk_movie_title_release` (`title`,`release_date`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='電影';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `movie`
--

LOCK TABLES `movie` WRITE;
/*!40000 ALTER TABLE `movie` DISABLE KEYS */;
INSERT INTO `movie` VALUES (1,'星際探險隊',128,'普遍級','西元 2150 年，一支由五名年輕太空人組成的探險隊，奉命前往銀河邊緣調查一顆突然發出訊號的神秘行星。旅途中他們必須面對隕石風暴、失靈的導航系統，以及彼此之間的不信任，最後才發現這顆行星的秘密關係著地球的未來。','陳志遠','林子翔, 許雅晴, 王大衛, 張美玲','科幻','2026-09-18','/videos/movies/1/trailer.mp4',1,'2026-11-01 09:51:21'),(2,'午夜偵探',112,'輔12級','一場暴雨夜裡，城市知名收藏家離奇失蹤，只留下一張寫著謎語的卡片。退休偵探與他的少年助手被捲入案件，在午夜的街道與屋頂之間追查線索，卻發現真相比想像中更接近自己。','黃啟明','周文傑, 李心怡, 張家豪','懸疑','2026-09-25','/videos/movies/2/trailer.mp4',1,'2026-11-01 09:51:21'),(3,'海洋之歌',95,'普遍級','住在海邊小鎮的女孩小海，某天撿到一個會發光的貝殼，從此能聽見海洋生物的歌聲。為了拯救即將消失的珊瑚礁，她跟著一隻老海龜潛入深海，展開一段奇妙又溫暖的冒險。','吳佳蓉','（配音）陳小雨, 劉建宏, 蔡依涵','動畫','2026-09-11','/videos/movies/3/trailer.mp4',1,'2026-11-01 09:51:21'),(4,'時光咖啡館',106,'保護級','巷弄裡有一間只在雨天營業的咖啡館，據說喝下老闆特調的咖啡，就能回到人生中最想重來的那一天。三位素不相識的客人，各自帶著遺憾推開了店門。','林思妤','郭書豪, 何晴, 蘇柏宇','劇情','2026-11-20','/videos/movies/4/trailer.mp4',1,'2026-11-01 09:51:21'),(5,'鋼鐵守護者',135,'輔15級','近未來的城市被犯罪集團控制，前特種部隊隊員穿上自己打造的鋼鐵裝甲，在法律與正義之間做出選擇，守護他最重要的家人與這座城市。','鄭國威','杜子安, 葉欣妍, 馬克·陳','動作','2026-12-18','/videos/movies/5/trailer.mp4',1,'2026-11-01 09:51:21');
/*!40000 ALTER TABLE `movie` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `movie_image`
--

DROP TABLE IF EXISTS `movie_image`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `movie_image` (
  `image_id` int NOT NULL AUTO_INCREMENT COMMENT '圖片編號',
  `movie_id` int NOT NULL COMMENT '所屬電影',
  `image_url` varchar(500) NOT NULL COMMENT '圖片路徑，例如 /images/movies/1/poster.jpg',
  `image_type` varchar(20) NOT NULL DEFAULT 'POSTER' COMMENT 'POSTER=海報、STILL=劇照、BANNER=橫幅',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '顯示順序，小的在前',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上傳時間',
  PRIMARY KEY (`image_id`),
  KEY `idx_movie_image_movie` (`movie_id`),
  CONSTRAINT `fk_movie_image_movie` FOREIGN KEY (`movie_id`) REFERENCES `movie` (`movie_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='電影圖片';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `movie_image`
--

LOCK TABLES `movie_image` WRITE;
/*!40000 ALTER TABLE `movie_image` DISABLE KEYS */;
INSERT INTO `movie_image` VALUES (1,1,'/images/movies/1/poster1.jpg','POSTER',0,'2026-11-01 09:51:21'),(2,1,'/images/movies/1/still1.jpg','STILL',1,'2026-11-01 09:51:21'),(3,1,'/images/movies/1/still2.jpg','STILL',2,'2026-11-01 09:51:21'),(4,1,'/images/movies/1/banner1.jpg','BANNER',0,'2026-11-01 09:51:21'),(5,2,'/images/movies/2/poster1.jpg','POSTER',0,'2026-11-01 09:51:21'),(6,2,'/images/movies/2/still1.jpg','STILL',1,'2026-11-01 09:51:21'),(7,2,'/images/movies/2/still2.jpg','STILL',2,'2026-11-01 09:51:21'),(8,2,'/images/movies/2/banner1.jpg','BANNER',0,'2026-11-01 09:51:21'),(9,3,'/images/movies/3/poster1.jpg','POSTER',0,'2026-11-01 09:51:21'),(10,3,'/images/movies/3/still1.jpg','STILL',1,'2026-11-01 09:51:21'),(11,3,'/images/movies/3/still2.jpg','STILL',2,'2026-11-01 09:51:21'),(12,3,'/images/movies/3/banner1.jpg','BANNER',0,'2026-11-01 09:51:21'),(13,4,'/images/movies/4/poster1.jpg','POSTER',0,'2026-11-01 09:51:21'),(14,4,'/images/movies/4/still1.jpg','STILL',1,'2026-11-01 09:51:21'),(15,4,'/images/movies/4/still2.jpg','STILL',2,'2026-11-01 09:51:21'),(16,4,'/images/movies/4/banner1.jpg','BANNER',0,'2026-11-01 09:51:21'),(17,5,'/images/movies/5/poster1.jpg','POSTER',0,'2026-11-01 09:51:21'),(18,5,'/images/movies/5/still1.jpg','STILL',1,'2026-11-01 09:51:21'),(19,5,'/images/movies/5/still2.jpg','STILL',2,'2026-11-01 09:51:21'),(20,5,'/images/movies/5/banner1.jpg','BANNER',0,'2026-11-01 09:51:21');
/*!40000 ALTER TABLE `movie_image` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `orders`
--

DROP TABLE IF EXISTS `orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orders` (
  `order_id` int NOT NULL AUTO_INCREMENT,
  `member_id` int NOT NULL,
  `showtime_id` int NOT NULL,
  `original_amount` int NOT NULL COMMENT '原價小計',
  `discount_amount` int NOT NULL COMMENT '折扣金額',
  `total_amount` int NOT NULL COMMENT '合計金額(實收)',
  `order_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`order_id`),
  KEY `idx_orders_time` (`order_time`),
  KEY `fk_orders_member` (`member_id`),
  KEY `fk_orders_showtime` (`showtime_id`),
  CONSTRAINT `fk_orders_member` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`),
  CONSTRAINT `fk_orders_showtime` FOREIGN KEY (`showtime_id`) REFERENCES `showtime` (`showtime_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='訂單';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orders`
--

LOCK TABLES `orders` WRITE;
/*!40000 ALTER TABLE `orders` DISABLE KEYS */;
INSERT INTO `orders` VALUES (1,1,1,600,0,600,'2026-11-01 10:05:00'),(2,1,2,600,180,420,'2026-11-01 11:20:00'),(3,1,3,900,180,720,'2026-11-02 09:40:00'),(4,1,4,600,60,540,'2026-11-02 15:10:00'),(5,1,5,600,30,570,'2026-11-03 20:30:00');
/*!40000 ALTER TABLE `orders` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `showtime`
--

DROP TABLE IF EXISTS `showtime`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `showtime` (
  `showtime_id` int NOT NULL AUTO_INCREMENT,
  `movie_id` int NOT NULL,
  `hall_id` int NOT NULL,
  `show_date` date NOT NULL,
  `start_time` time NOT NULL,
  PRIMARY KEY (`showtime_id`),
  UNIQUE KEY `uk_showtime_hall_time` (`hall_id`,`show_date`,`start_time`),
  KEY `idx_showtime_movie` (`movie_id`),
  CONSTRAINT `fk_showtime_hall` FOREIGN KEY (`hall_id`) REFERENCES `hall` (`hall_id`),
  CONSTRAINT `fk_showtime_movie` FOREIGN KEY (`movie_id`) REFERENCES `movie` (`movie_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='場次';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `showtime`
--

LOCK TABLES `showtime` WRITE;
/*!40000 ALTER TABLE `showtime` DISABLE KEYS */;
INSERT INTO `showtime` VALUES (1,1,1,'2026-11-10','13:00:00'),(2,1,2,'2026-11-10','19:30:00'),(3,2,1,'2026-11-11','14:00:00'),(4,3,3,'2026-11-12','10:30:00'),(5,4,2,'2026-11-21','19:00:00'),(6,5,3,'2026-12-20','20:00:00');
/*!40000 ALTER TABLE `showtime` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ticket`
--

DROP TABLE IF EXISTS `ticket`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ticket` (
  `ticket_id` int NOT NULL AUTO_INCREMENT,
  `order_id` int NOT NULL,
  `showtime_id` int NOT NULL,
  `row_no` int NOT NULL COMMENT '第幾排(1起算)',
  `seat_no` int NOT NULL COMMENT '第幾號(1起算)',
  `type_id` int NOT NULL,
  `original_price` int NOT NULL,
  `price` int NOT NULL COMMENT '實際售價',
  PRIMARY KEY (`ticket_id`),
  UNIQUE KEY `uk_ticket_seat` (`showtime_id`,`row_no`,`seat_no`),
  KEY `idx_ticket_order` (`order_id`),
  KEY `fk_ticket_type` (`type_id`),
  CONSTRAINT `fk_ticket_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`order_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_ticket_showtime` FOREIGN KEY (`showtime_id`) REFERENCES `showtime` (`showtime_id`),
  CONSTRAINT `fk_ticket_type` FOREIGN KEY (`type_id`) REFERENCES `ticket_type` (`type_id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='電影票';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ticket`
--

LOCK TABLES `ticket` WRITE;
/*!40000 ALTER TABLE `ticket` DISABLE KEYS */;
INSERT INTO `ticket` VALUES (1,1,1,5,4,2,300,300),(2,1,1,5,5,2,300,300),(3,2,2,6,7,1,300,150),(4,2,2,6,8,5,300,270),(5,3,3,8,3,3,300,240),(6,3,3,8,4,3,300,240),(7,3,3,8,5,3,300,240),(8,4,4,9,2,4,600,540),(9,5,5,7,9,2,300,300),(10,5,5,7,10,5,300,270);
/*!40000 ALTER TABLE `ticket` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ticket_type`
--

DROP TABLE IF EXISTS `ticket_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ticket_type` (
  `type_id` int NOT NULL AUTO_INCREMENT,
  `type_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `base_price` int NOT NULL COMMENT '原價',
  `discount_rate` decimal(3,2) NOT NULL DEFAULT '1.00' COMMENT '折扣率',
  `description` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`type_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='票種';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ticket_type`
--

LOCK TABLES `ticket_type` WRITE;
/*!40000 ALTER TABLE `ticket_type` DISABLE KEYS */;
INSERT INTO `ticket_type` VALUES (1,'VIP票',150,0.50,'一般票價格*5折'),(2,'一般票',300,1.00,'不打折'),(3,'團體票',240,0.80,'一般票價格*8折'),(4,'雙人票',540,0.90,'一般票價格*9折'),(5,'學生票',270,0.90,'一般票價格*9折');
/*!40000 ALTER TABLE `ticket_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping events for database 'movie_pos'
--

--
-- Dumping routines for database 'movie_pos'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-11-01 12:20:25
