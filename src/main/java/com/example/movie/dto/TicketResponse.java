package com.example.movie.dto;

import com.example.movie.model.Ticket;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@JsonPropertyOrder({ "ticketId", "orderId", "showtimeId", "rowNo", "seatNo",
	"typeId", "typeName", "originalPrice", "price" })
@Data
public class TicketResponse {

	private Integer ticketId;
	private Integer orderId;
	private Integer showtimeId;
	private Integer rowNo;
	private Integer seatNo;
	private Integer typeId;
	private String typeName;
	private Integer originalPrice;
	private Integer price;

	// 用關聯物件取 id：剛新增的票，唯讀的 orderId、showtimeId、typeId 還沒從 DB 讀回來
	public static TicketResponse from(Ticket t)
	{
		TicketResponse res = new TicketResponse();
		res.setTicketId(t.getTicketId());
		res.setOrderId(t.getOrder().getOrderId());
		res.setShowtimeId(t.getShowtime().getShowtimeId());
		res.setRowNo(t.getRowNo());
		res.setSeatNo(t.getSeatNo());
		res.setTypeId(t.getTicketType().getTypeId());
		res.setTypeName(t.getTicketType().getTypeName());
		res.setOriginalPrice(t.getOriginalPrice());
		res.setPrice(t.getPrice());
		return res;
	}
}
