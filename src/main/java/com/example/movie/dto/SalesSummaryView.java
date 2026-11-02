package com.example.movie.dto;

// 期間銷售總計（介面投影）
public interface SalesSummaryView {
	Long getOrderCount();
	Long getTicketCount();
	Long getOriginalAmount();
	Long getDiscountAmount();
	Long getTotalAmount();
}
