-- =====================================================================
-- movie_pos 資料庫更新：電影簡介欄位 + 電影圖片資料表
-- 執行方式：MySQL Workbench 開啟本檔 → 按「閃電」圖示（Execute）全部執行
-- 特性：可以重複執行。已存在的欄位/資料表會自動略過，不會刪除任何資料。
-- =====================================================================

USE movie_pos;

-- ---------------------------------------------------------------------
-- 步驟 1：建立暫時用的小工具（預存程序）
--   功能：如果 movie 表還沒有這個欄位才新增；已經有就略過。
--   （MySQL 8 的 ALTER TABLE 不支援 ADD COLUMN IF NOT EXISTS，所以自己寫）
-- ---------------------------------------------------------------------
DROP PROCEDURE IF EXISTS add_column_if_missing;

DELIMITER $$
CREATE PROCEDURE add_column_if_missing(
    IN p_table  VARCHAR(64),     -- 資料表名稱
    IN p_column VARCHAR(64),     -- 欄位名稱
    IN p_def    VARCHAR(500)     -- 欄位定義，例如 'TEXT NULL'
)
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME   = p_table
          AND COLUMN_NAME  = p_column
    ) THEN
        SET @ddl = CONCAT('ALTER TABLE `', p_table, '` ADD COLUMN `', p_column, '` ', p_def);
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
        SELECT CONCAT('已新增欄位 ', p_table, '.', p_column) AS 結果;
    ELSE
        SELECT CONCAT('欄位已存在，略過 ', p_table, '.', p_column) AS 結果;
    END IF;
END$$
DELIMITER ;

-- ---------------------------------------------------------------------
-- 步驟 2：在 movie 表加上「電影簡介」相關欄位
-- ---------------------------------------------------------------------
CALL add_column_if_missing('movie', 'synopsis',     'TEXT NULL COMMENT ''電影簡介''');
CALL add_column_if_missing('movie', 'director',     'VARCHAR(100) NULL COMMENT ''導演''');
CALL add_column_if_missing('movie', 'actors',       'VARCHAR(255) NULL COMMENT ''主要演員，以逗號分隔''');
CALL add_column_if_missing('movie', 'genre',        'VARCHAR(50) NULL COMMENT ''類型：動作、喜劇、動畫…''');
CALL add_column_if_missing('movie', 'age_rating',   'VARCHAR(10) NULL COMMENT ''分級：普遍級/保護級/輔12級/輔15級/限制級''');
CALL add_column_if_missing('movie', 'release_date', 'DATE NULL COMMENT ''上映日期''');
CALL add_column_if_missing('movie', 'trailer_url',  'VARCHAR(500) NULL COMMENT ''預告片網址''');
CALL add_column_if_missing('movie', 'is_active',    'TINYINT(1) NOT NULL DEFAULT 1 COMMENT ''1=前台顯示，0=下架隱藏''');
CALL add_column_if_missing('movie', 'updated_at',   'DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT ''最後修改時間''');

-- ---------------------------------------------------------------------
-- 步驟 3：建立 movie_image（電影圖片）資料表
--   一部電影可以有多張圖片（海報、劇照、橫幅）→ 一對多
--   圖片檔案放在伺服器資料夾，資料庫只存「路徑/網址」
--
--   外鍵 movie_id 必須和 movie 表主鍵「型別完全相同」，
--   所以這裡先自動查出 movie 的主鍵名稱與型別，再組出 CREATE TABLE。
-- ---------------------------------------------------------------------
SET @pk_col := (
    SELECT COLUMN_NAME FROM information_schema.KEY_COLUMN_USAGE
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'movie' AND CONSTRAINT_NAME = 'PRIMARY'
    ORDER BY ORDINAL_POSITION LIMIT 1
);
SET @pk_type := (
    SELECT COLUMN_TYPE FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'movie' AND COLUMN_NAME = @pk_col
);
SELECT @pk_col AS movie主鍵欄位, @pk_type AS 主鍵型別;   -- 執行後可在結果看到

SET @ddl := CONCAT(
  'CREATE TABLE IF NOT EXISTS movie_image (',
  '  image_id    INT          NOT NULL AUTO_INCREMENT COMMENT ''圖片編號'',',
  '  movie_id    ', @pk_type, ' NOT NULL COMMENT ''所屬電影'',',
  '  image_url   VARCHAR(500) NOT NULL COMMENT ''圖片路徑，例如 /images/movies/1/poster.jpg'',',
  '  image_type  VARCHAR(20)  NOT NULL DEFAULT ''POSTER'' COMMENT ''POSTER=海報、STILL=劇照、BANNER=橫幅'',',
  '  sort_order  INT          NOT NULL DEFAULT 0 COMMENT ''顯示順序，小的在前'',',
  '  created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT ''上傳時間'',',
  '  PRIMARY KEY (image_id),',
  '  KEY idx_movie_image_movie (movie_id),',
  '  CONSTRAINT fk_movie_image_movie FOREIGN KEY (movie_id) REFERENCES movie(`', @pk_col, '`)',
  '      ON DELETE CASCADE ON UPDATE CASCADE',
  ') ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT=''電影圖片'''
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ---------------------------------------------------------------------
-- 步驟 4：刪除暫時工具
-- ---------------------------------------------------------------------
DROP PROCEDURE IF EXISTS add_column_if_missing;

-- ---------------------------------------------------------------------
-- 步驟 5：檢查結果
-- ---------------------------------------------------------------------
SHOW COLUMNS FROM movie;
SHOW COLUMNS FROM movie_image;
