package com.example.movie.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.example.movie.model.ShowTime;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

// 新增 / 修改場次時，前端送來的資料（不含 showtimeId）
@Data
public class ShowtimeRequest {

	@NotNull(message = "請選擇電影")
	private Integer movieId;

	@NotNull(message = "請選擇影廳")
	private Integer hallId;

	// 格式 yyyy-MM-dd，例如 2026-11-10
	@NotNull(message = "請輸入放映日期")
	private LocalDate showDate;

	// 格式 HH:mm 或 HH:mm:ss，例如 19:30
	@NotNull(message = "請輸入開演時間")
	private LocalTime startTime;

	// movieId、hallId 先放在唯讀欄位傳給 Service，Service 會查出 Movie、Hall 再設定關聯
	public ShowTime toEntity()
	{
		ShowTime s = new ShowTime();
		s.setMovieId(movieId);
		s.setHallId(hallId);
		s.setShowDate(showDate);
		s.setStartTime(startTime);
		return s;
	}
}
