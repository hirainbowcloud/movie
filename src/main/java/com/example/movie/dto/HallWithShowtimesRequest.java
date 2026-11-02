package com.example.movie.dto;

import java.util.List;

import com.example.movie.model.ShowTime;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class HallWithShowtimesRequest {

	@NotNull(message = "請輸入影廳資料")
	@Valid
	private HallRequest hall;
	
	@NotEmpty(message = "至少要有一個場次")
	@Valid
	private List<HallShowtimeItemRequest> showtimes;
	
	public List<ShowTime> toShowtimeEntities()
	{
		return showtimes.stream()
				.map(HallShowtimeItemRequest::toEntity)
				.toList();
	}
}
