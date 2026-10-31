package com.example.movie.dto;

import com.example.movie.model.Hall;

public record HallResponse
(
	Integer hallId,
	String hallName,
	Integer rowCount,
	Integer firstRowSeats
)
{
	public static HallResponse from(Hall h)
	{
		return new HallResponse
				(
				h.getHallId(),
				h.getHallName(),
				h.getRowCount(),
				h.getFirstRowSeats()
				);
	}
}