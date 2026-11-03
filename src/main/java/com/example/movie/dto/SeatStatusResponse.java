package com.example.movie.dto;

// 一個座位的狀態：第幾排、第幾號、是否已售出
// status：SOLD = 已售出，AVAILABLE = 未售出
public record SeatStatusResponse(Integer rowNo, Integer seatNo, String status) {

	public static final String SOLD = "SOLD";
	public static final String AVAILABLE = "AVAILABLE";
}
