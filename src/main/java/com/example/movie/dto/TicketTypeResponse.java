package com.example.movie.dto;

import java.math.BigDecimal;

import com.example.movie.model.TicketType;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@JsonPropertyOrder({ "typeId", "typeName", "basePrice", "discountRate", "description" })
@Data
public class TicketTypeResponse {

	private Integer typeId;
	private String typeName;
	private Integer basePrice;
	private BigDecimal discountRate;
	private String description;

	public static TicketTypeResponse from(TicketType t)
	{
		TicketTypeResponse res = new TicketTypeResponse();
		res.setTypeId(t.getTypeId());
		res.setTypeName(t.getTypeName());
		res.setBasePrice(t.getBasePrice());
		res.setDiscountRate(t.getDiscountRate());
		res.setDescription(t.getDescription());
		return res;
	}
}
