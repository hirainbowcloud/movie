package com.example.movie.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.example.movie.model.ShowTime;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@JsonPropertyOrder({ "showtimeId", "movieId", "movieTitle", "hallId", "hallName", "showDate", "startTime" })
@Data
public class ShowtimeResponse {

	private Integer showtimeId;
	private Integer movieId;
	private String movieTitle;
	private Integer hallId;
	private String hallName;
	private LocalDate showDate;
	private LocalTime startTime;

	// 用 getMovie()、getHall() 取值：剛新增的場次，唯讀的 movieId、hallId 還沒從 DB 讀回來
	public static ShowtimeResponse from(ShowTime s)
	{
		ShowtimeResponse res = new ShowtimeResponse();
		res.setShowtimeId(s.getShowtimeId());
		res.setMovieId(s.getMovie().getMovieId());
		res.setMovieTitle(s.getMovie().getTitle());
		res.setHallId(s.getHall().getHallId());
		res.setHallName(s.getHall().getHallName());
		res.setShowDate(s.getShowDate());
		res.setStartTime(s.getStartTime());
		return res;
	}
}
