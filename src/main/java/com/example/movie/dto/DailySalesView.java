package com.example.movie.dto;

// 每日銷售（介面投影）；saleDate 格式 yyyy-MM-dd
public interface DailySalesView {
	String getSaleDate();
	Long getOrderCount();
	Long getTicketCount();
	Long getTotalAmount();
}
