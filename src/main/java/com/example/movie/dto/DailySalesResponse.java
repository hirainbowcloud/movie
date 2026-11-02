package com.example.movie.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@JsonPropertyOrder({ "saleDate", "orderCount", "ticketCount", "totalAmount" })
@Data
public class DailySalesResponse {

	private String saleDate;
	private Long orderCount;
	private Long ticketCount;
	private Long totalAmount;

	public static DailySalesResponse from(DailySalesView v)
	{
		DailySalesResponse res = new DailySalesResponse();
		res.setSaleDate(v.getSaleDate());
		res.setOrderCount(v.getOrderCount());
		res.setTicketCount(v.getTicketCount());
		res.setTotalAmount(v.getTotalAmount());
		return res;
	}
}
