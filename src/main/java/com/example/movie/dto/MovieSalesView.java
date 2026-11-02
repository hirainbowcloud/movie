package com.example.movie.dto;

// 介面投影（Projection）：Spring Data 依 SQL 的欄位別名自動實作這些 getter
// 別名 movieId → getMovieId()、ticketCount → getTicketCount()
public interface MovieSalesView {
	Integer getMovieId();
	String getTitle();
	Long getTicketCount();
	Long getTotalAmount();
}
