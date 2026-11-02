package com.example.movie.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.movie.dao.ReportService;
import com.example.movie.dto.MovieSalesResponse;
import com.example.movie.dto.SalesReportResponse;

// 管理者報表
@RestController
@RequestMapping("/api/admin/report")
public class AdminReportController {

	@Autowired
	ReportService reportSrv;

	// 電影銷售張數與金額
	// GET /api/admin/report/movie-sales            → 全部電影
	// GET /api/admin/report/movie-sales?title=星際 → 片名包含「星際」的電影
	@GetMapping("/movie-sales")
	public ResponseEntity<List<MovieSalesResponse>> movieSales
	(@RequestParam(required = false) String title)
	{
		List<MovieSalesResponse> result = reportSrv.findMovieSales(title);
		if (result.isEmpty()) {
			return ResponseEntity.notFound().build(); // 找不到這個片名
		}
		return ResponseEntity.ok(result);
	}

	// 本週 / 本月銷售總表
	// GET /api/admin/report/sales?period=week
	// GET /api/admin/report/sales?period=month
	@GetMapping("/sales")
	public ResponseEntity<SalesReportResponse> sales
	(@RequestParam(defaultValue = "week") String period)
	{
		return ResponseEntity.ok(reportSrv.findSalesReport(period));
	}
}
