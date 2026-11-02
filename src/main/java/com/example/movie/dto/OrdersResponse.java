package com.example.movie.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import com.example.movie.model.Orders;
import com.example.movie.model.ShowTime;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@JsonPropertyOrder({ "orderId", "memberId", "showtimeId", "originalAmount",
	"discountAmount", "totalAmount","movieTitle", "hallName","showDate","startTime" ,"orderTime", "tickets" })
@Data
public class OrdersResponse {

	private Integer orderId;
	private Integer memberId;
	private Integer showtimeId;
	private Integer originalAmount;
	private Integer discountAmount;
	private Integer totalAmount;
	private LocalDateTime orderTime;
	private List<TicketResponse> tickets;
	
	//我的訂單用
	private String movieTitle;
	private String hallName;
	private LocalDate showDate;
	private LocalTime startTime;

	public static OrdersResponse from(Orders o)
	{
		OrdersResponse res = new OrdersResponse();
		res.setOrderId(o.getOrderId());
		res.setMemberId(o.getMember().getMemberId());
		res.setShowtimeId(o.getShowtime().getShowtimeId());
		res.setOriginalAmount(o.getOriginalAmount());
		res.setDiscountAmount(o.getDiscountAmount());
		res.setTotalAmount(o.getTotalAmount());
		res.setOrderTime(o.getOrderTime());
		res.setTickets(o.getTickets()
				.stream()
				.map(TicketResponse::from)
				.toList());
		
		//我的訂單用
		ShowTime s = o.getShowtime();
		res.setMovieTitle(s.getMovie().getTitle());
		res.setHallName(s.getHall().getHallName());
		res.setShowDate(s.getShowDate());
		res.setStartTime(s.getStartTime());
		return res;
	}
}
