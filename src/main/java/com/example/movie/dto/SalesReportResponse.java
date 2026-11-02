package com.example.movie.dto;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

// 本週 / 本月銷售總表
@JsonPropertyOrder({ "period", "startDate", "endDate",
	"orderCount", "ticketCount", "originalAmount", "discountAmount", "totalAmount",
	"daily", "movies" })
@Data
public class SalesReportResponse {

	private String period;          // week / month
	private LocalDate startDate;    // 期間第一天
	private LocalDate endDate;      // 期間最後一天（包含）

	private Long orderCount;        // 訂單數
	private Long ticketCount;       // 票數
	private Long originalAmount;    // 原價小計
	private Long discountAmount;    // 折扣金額
	private Long totalAmount;       // 實收金額

	private List<DailySalesResponse> daily;   // 每日明細
	private List<MovieSalesResponse> movies;  // 各電影銷售
}
