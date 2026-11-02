package com.example.movie.Repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import com.example.movie.dto.DailySalesView;
import com.example.movie.dto.MovieSalesView;
import com.example.movie.dto.SalesSummaryView;
import com.example.movie.model.Orders;

// 報表專用：只有查詢，所以繼承最基本的 Repository（沒有 save、delete）
// 統計用原生 SQL + 介面投影（XxxView）：查詢結果的欄位別名，對應 View 介面的 getXxx()
// SUM() 在 MySQL 回傳 DECIMAL，用 CAST(... AS SIGNED) 轉成整數，Java 才能用 Long 接
public interface ReportRepository extends Repository<Orders, Integer> {

	// ===================== 1. 電影銷售統計 =====================
	// LEFT JOIN：沒賣出任何票的電影也會出現（張數 0、金額 0）
	// title 是空字串 → LIKE '%%' 會符合全部電影；有值 → 片名模糊查詢
	@Query(value = """
			SELECT m.movie_id AS movieId,
			       m.title    AS title,
			       CAST(COUNT(t.ticket_id)        AS SIGNED) AS ticketCount,
			       CAST(COALESCE(SUM(t.price), 0) AS SIGNED) AS totalAmount
			FROM movie m
			LEFT JOIN showtime s ON s.movie_id    = m.movie_id
			LEFT JOIN ticket   t ON t.showtime_id = s.showtime_id
			WHERE m.title LIKE CONCAT('%', :title, '%')
			GROUP BY m.movie_id, m.title
			ORDER BY totalAmount DESC, m.movie_id
			""", nativeQuery = true)
	List<MovieSalesView> findMovieSales(@Param("title") String title);

	// ===================== 2. 期間銷售總表 =====================
	// 期間條件：start <= order_time < end（end 不包含，例如本月 = 11/1 00:00 ~ 12/1 00:00）

	// 2-1. 總計：訂單數、票數、原價、折扣、實收
	@Query(value = """
			SELECT CAST(COUNT(*) AS SIGNED)                           AS orderCount,
			       CAST(COALESCE(SUM(o.original_amount), 0) AS SIGNED) AS originalAmount,
			       CAST(COALESCE(SUM(o.discount_amount), 0) AS SIGNED) AS discountAmount,
			       CAST(COALESCE(SUM(o.total_amount), 0)    AS SIGNED) AS totalAmount,
			       CAST((SELECT COUNT(*)
			             FROM ticket t JOIN orders o2 ON o2.order_id = t.order_id
			             WHERE o2.order_time >= :start AND o2.order_time < :end) AS SIGNED) AS ticketCount
			FROM orders o
			WHERE o.order_time >= :start AND o.order_time < :end
			""", nativeQuery = true)
	SalesSummaryView findSalesSummary(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

	// 2-2. 每日明細（只列出有訂單的日期）
	//      子查詢 d：每筆訂單一列（日期、實收、票數）→ 外層再依日期加總
	@Query(value = """
			SELECT d.saleDate                                   AS saleDate,
			       CAST(COUNT(*) AS SIGNED)                     AS orderCount,
			       CAST(COALESCE(SUM(d.ticketCount), 0) AS SIGNED) AS ticketCount,
			       CAST(COALESCE(SUM(d.totalAmount), 0) AS SIGNED) AS totalAmount
			FROM (
			    SELECT DATE_FORMAT(o.order_time, '%Y-%m-%d') AS saleDate,
			           o.total_amount                        AS totalAmount,
			           (SELECT COUNT(*) FROM ticket t WHERE t.order_id = o.order_id) AS ticketCount
			    FROM orders o
			    WHERE o.order_time >= :start AND o.order_time < :end
			) d
			GROUP BY d.saleDate
			ORDER BY d.saleDate
			""", nativeQuery = true)
	List<DailySalesView> findDailySales(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

	// 2-3. 期間內各電影的銷售（只列出有賣出的電影）
	@Query(value = """
			SELECT m.movie_id AS movieId,
			       m.title    AS title,
			       CAST(COUNT(t.ticket_id)        AS SIGNED) AS ticketCount,
			       CAST(COALESCE(SUM(t.price), 0) AS SIGNED) AS totalAmount
			FROM orders o
			JOIN ticket   t ON t.order_id    = o.order_id
			JOIN showtime s ON s.showtime_id = t.showtime_id
			JOIN movie    m ON m.movie_id    = s.movie_id
			WHERE o.order_time >= :start AND o.order_time < :end
			GROUP BY m.movie_id, m.title
			ORDER BY totalAmount DESC, m.movie_id
			""", nativeQuery = true)
	List<MovieSalesView> findMovieSalesBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
