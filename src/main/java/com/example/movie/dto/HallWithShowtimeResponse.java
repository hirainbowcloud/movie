package com.example.movie.dto;

import java.util.List;

import lombok.Data;

@Data
public class HallWithShowtimeResponse {

	private HallResponse hall;
	private List<ShowtimeResponse> showtimes;
}
