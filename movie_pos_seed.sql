-- =====================================================================
-- movie_pos 測試資料：電影簡介 + 圖片 + 預告片
-- 前提：已執行過 movie_pos_update.sql
-- 執行方式：MySQL Workbench 開啟本檔 → 按第一個閃電圖示（Execute）
-- 可以重複執行：電影用 ON DUPLICATE KEY UPDATE，圖片先刪再新增
--
-- 圖片/影片檔案在 D:\webJava\movie_assets\static\ 底下
-- 之後放進 Spring Boot 專案的 src/main/resources/static/，
-- 網址就會是 http://localhost:8080/images/movies/1/poster.jpg
-- =====================================================================

USE movie_pos;

-- ---------------------------------------------------------------------
-- 1. 電影資料
--    1~3 是原本就有的電影：只補上新欄位（片名、片長、分級不變）
--    4 是「即將上映」、5 是「已下架（is_active=0）」，用來測試前台篩選
-- ---------------------------------------------------------------------
INSERT INTO movie (movie_id, title, duration_min, rating,
                   synopsis, director, actors, genre, release_date, trailer_url, is_active)
VALUES
(1, '星際探險隊', 128, '普遍級',
 '西元 2150 年，一支由五名年輕太空人組成的探險隊，奉命前往銀河邊緣調查一顆突然發出訊號的神秘行星。旅途中，他們必須面對隕石風暴、失靈的導航系統，以及彼此之間的不信任。當真相揭曉，他們才發現這顆行星的秘密，關係著地球的未來。',
 '陳志遠', '林子翔, 許雅晴, 王大衛, 張美玲', '科幻',
 '2026-09-18', '/videos/movies/1/trailer.mp4', 1),

(2, '午夜偵探', 112, '輔12級',
 '神出鬼沒的怪盜「夜鴉」寄出預告函：今夜午夜十二時，將取走美術館的鎮館之寶「午夜之星」。戴著格紋獵鹿帽的少女偵探林小鹿，憑著過人的觀察力與一支老放大鏡，在探照燈交錯的屋頂上展開追逐。真相揭曉時，她發現怪盜的目的，竟是為了揭開一樁塵封二十年的懸案。',
 '黃啟明', '（配音）許小鹿, 周文傑, 李心怡', '懸疑',
 '2026-09-25', '/videos/movies/2/trailer.mp4', 1),

(3, '海洋之歌', 95, '普遍級',
 '住在海邊小鎮的女孩小海，某天撿到一個會唱歌的貝殼。跟著歌聲，她潛入海底，結識了一群熱愛音樂的魚兒。為了拯救被污染威脅的珊瑚礁，小海和海洋朋友們決定舉辦一場海底音樂會，喚起人類對海洋的關心。適合全家大小一起觀賞的溫馨動畫。',
 '吳佳蓉', '（配音）陳小雨, 劉建宏, 蔡依涵', '動畫',
 '2026-09-11', '/videos/movies/3/trailer.mp4', 1),

(4, '時光咖啡館', 106, '保護級',
 '巷弄裡有一間只在雨天營業的咖啡館，據說喝下老闆特調的咖啡，就能回到人生中最後悔的那一天。失戀的插畫家阿晴誤打誤撞走進店裡，她想改變過去，卻發現每一次改變，都讓現在的自己失去更多。一部關於珍惜當下的浪漫愛情電影。',
 '林思妤', '郭書豪, 何晴, 蘇柏宇', '愛情',
 '2026-11-20', '/videos/movies/4/trailer.mp4', 1),

(5, '鋼鐵守護者', 135, '輔15級',
 '近未來的城市被犯罪集團控制，前特種部隊隊員雷剛穿上自己打造的鋼鐵裝甲，成為守護市民的神秘英雄。當集團首腦綁架了他的家人，雷剛必須在二十四小時內闖入戒備森嚴的總部。動作場面火爆、節奏緊湊的年度動作鉅片。',
 '鄭國威', '杜子安, 葉欣妍, 馬克·陳', '動作',
 '2026-12-18', '/videos/movies/5/trailer.mp4', 0)
AS new
ON DUPLICATE KEY UPDATE
    synopsis     = new.synopsis,
    director     = new.director,
    actors       = new.actors,
    genre        = new.genre,
    release_date = new.release_date,
    trailer_url  = new.trailer_url,
    is_active    = new.is_active;

-- ---------------------------------------------------------------------
-- 2. 電影圖片：每部電影 4 張（海報 1、劇照 2、橫幅 1）
-- ---------------------------------------------------------------------
DELETE FROM movie_image WHERE movie_id IN (1, 2, 3, 4, 5);

INSERT INTO movie_image (movie_id, image_url, image_type, sort_order) VALUES
(1, '/images/movies/1/poster.jpg', 'POSTER', 0),
(1, '/images/movies/1/still1.jpg', 'STILL',  1),
(1, '/images/movies/1/still2.jpg', 'STILL',  2),
(1, '/images/movies/1/banner.jpg', 'BANNER', 0),
(2, '/images/movies/2/poster.jpg', 'POSTER', 0),
(2, '/images/movies/2/still1.jpg', 'STILL',  1),
(2, '/images/movies/2/still2.jpg', 'STILL',  2),
(2, '/images/movies/2/banner.jpg', 'BANNER', 0),
(3, '/images/movies/3/poster.jpg', 'POSTER', 0),
(3, '/images/movies/3/still1.jpg', 'STILL',  1),
(3, '/images/movies/3/still2.jpg', 'STILL',  2),
(3, '/images/movies/3/banner.jpg', 'BANNER', 0),
(4, '/images/movies/4/poster.jpg', 'POSTER', 0),
(4, '/images/movies/4/still1.jpg', 'STILL',  1),
(4, '/images/movies/4/still2.jpg', 'STILL',  2),
(4, '/images/movies/4/banner.jpg', 'BANNER', 0),
(5, '/images/movies/5/poster.jpg', 'POSTER', 0),
(5, '/images/movies/5/still1.jpg', 'STILL',  1),
(5, '/images/movies/5/still2.jpg', 'STILL',  2),
(5, '/images/movies/5/banner.jpg', 'BANNER', 0);

-- ---------------------------------------------------------------------
-- 3. 新電影 4、5 的場次（讓購票人可以測試；舊電影場次不動）
-- ---------------------------------------------------------------------
INSERT IGNORE INTO showtime (showtime_id, movie_id, hall_id, show_date, start_time) VALUES
(10, 4, 1, '2026-11-20', '13:00:00'),
(11, 4, 2, '2026-11-20', '19:30:00'),
(12, 5, 3, '2026-12-18', '20:00:00');

-- ---------------------------------------------------------------------
-- 4. 檢查結果
-- ---------------------------------------------------------------------
-- 管理者看到的：全部電影
SELECT movie_id, title, genre, rating, release_date, is_active,
       (SELECT COUNT(*) FROM movie_image i WHERE i.movie_id = m.movie_id) AS 圖片數
FROM movie m
ORDER BY movie_id;

-- 購票人看到的：上架中的電影 + 海報
SELECT m.movie_id, m.title, m.genre, m.rating, m.release_date, i.image_url AS poster
FROM movie m
LEFT JOIN movie_image i ON i.movie_id = m.movie_id AND i.image_type = 'POSTER'
WHERE m.is_active = 1
ORDER BY m.release_date;
