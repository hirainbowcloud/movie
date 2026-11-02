package com.example.movie.dao;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.movie.Repository.ReportRepository;
import com.example.movie.dto.DailySalesResponse;
import com.example.movie.dto.MovieSalesResponse;
import com.example.movie.dto.MovieSalesView;
import com.example.movie.dto.SalesReportResponse;
import com.example.movie.dto.SalesSummaryView;

// 管理者報表：只有查詢
// 報表本身就是統計結果（不是 Entity），所以這裡直接組成 Response 回傳
@Service
public class ReportService {

	@Autowired
	ReportRepository reportRepository;

	// ===================== 1. 電影銷售統計 =====================
	// title 沒傳或空白 → 全部電影；有值 → 片名模糊查詢
	@Transactional(readOnly = true)
	public List<MovieSalesResponse> findMovieSales(String title)
	{
		String keyword = (title == null) ? "" : title.trim(); // 空字串 = 全部電影
		return reportRepository.findMovieSales(keyword)
				.stream()
				.map(MovieSalesResponse::from)
				.toList();
	}

	// ===================== 2. 本週 / 本月銷售總表 =====================
	@Transactional(readOnly = true)
	public SalesReportResponse findSalesReport(String period)
	{
		LocalDate today = LocalDate.now();
		LocalDate start;
		LocalDate endExclusive; // 不包含這一天

		if ("week".equalsIgnoreCase(period))
		{
			// 本週：星期一 ~ 星期日
			start = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
			endExclusive = start.plusWeeks(1);
		}
		else if ("month".equalsIgnoreCase(period))
		{
			// 本月：1 號 ~ 月底
			start = today.withDayOfMonth(1);
			endExclusive = start.plusMonths(1);
		}
		else
		{
			throw new IllegalArgumentException("period 只能是 week 或 month"); // → 400
		}

		LocalDateTime from = start.atStartOfDay();        // 例如 2026-11-01 00:00
		LocalDateTime to = endExclusive.atStartOfDay();   // 例如 2026-12-01 00:00（不包含）

		SalesSummaryView sum = reportRepository.findSalesSummary(from, to);

		SalesReportResponse res = new SalesReportResponse();
		res.setPeriod(period.toLowerCase());
		res.setStartDate(start);
		res.setEndDate(endExclusive.minusDays(1));
		res.setOrderCount(sum.getOrderCount());
		res.setTicketCount(sum.getTicketCount());
		res.setOriginalAmount(sum.getOriginalAmount());
		res.setDiscountAmount(sum.getDiscountAmount());
		res.setTotalAmount(sum.getTotalAmount());
		res.setDaily(reportRepository.findDailySales(from, to)
				.stream()
				.map(DailySalesResponse::from)
				.toList());
		res.setMovies(reportRepository.findMovieSalesBetween(from, to)
				.stream()
				.map(MovieSalesResponse::from)
				.toList());
		return res;
	}
}
