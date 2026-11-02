package com.example.movie.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@JsonPropertyOrder({ "movieId", "title", "ticketCount", "totalAmount" })
@Data
public class MovieSalesResponse {

	private Integer movieId;
	private String title;
	private Long ticketCount;   // 銷售張數
	private Long totalAmount;   // 銷售金額（實收）

	public static MovieSalesResponse from(MovieSalesView v)
	{
		MovieSalesResponse res = new MovieSalesResponse();
		res.setMovieId(v.getMovieId());
		res.setTitle(v.getTitle());
		res.setTicketCount(v.getTicketCount());
		res.setTotalAmount(v.getTotalAmount());
		return res;
	}
}
