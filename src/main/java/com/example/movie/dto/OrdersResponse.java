package com.example.movie.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.example.movie.model.Orders;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@JsonPropertyOrder({ "orderId", "memberId", "showtimeId", "originalAmount",
	"discountAmount", "totalAmount", "orderTime", "tickets" })
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
		return res;
	}
}
