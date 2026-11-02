package com.example.movie.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.example.movie.model.ShowTime;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class HallShowtimeItemRequest {
	
	@NotNull(message = "請選擇電影")
	private Integer movieId;
	
	@NotNull(message = "請輸入放映日期")
	private LocalDate showDate;

	@NotNull(message = "請輸入開演時間")
	private LocalTime startTime;
	
	public ShowTime toEntity()
	{
		ShowTime s = new ShowTime();
		s.setMovieId(movieId);
		s.setShowDate(showDate);
		s.setStartTime(startTime);
		return s;
	}
}
