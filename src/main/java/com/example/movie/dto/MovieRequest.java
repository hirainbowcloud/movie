package com.example.movie.dto;

import java.time.LocalDate;

import com.example.movie.model.Movie;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

// 新增 / 修改電影時，前端送來的電影資料（不含 movieId、圖片、預告片網址）
@Data
public class MovieRequest {

	@NotBlank(message = "片名不可以空白")
	@Size(max = 100, message = "片名最多100字")
	private String title;

	@NotNull(message = "請輸入片長")
	@Min(value = 1, message = "片長至少1分鐘")
	private Integer durationMin;

	// 分級:普遍級/保護級
	@NotBlank(message = "請選擇分級")
	private String rating;

	// 電影簡介
	private String synopsis;

	// 導演
	@Size(max = 100)
	private String director;

	// 演員
	@Size(max = 255)
	private String actors;

	// 類型:動作、喜劇、動畫
	@Size(max = 50)
	private String genre;

	@NotNull(message = "請輸入上映日期")
	private LocalDate releaseDate;

	// 上下架（新增時沒傳 → 預設上架；修改時沒傳 → 維持原本狀態）
	private Boolean isActive;

	public Movie toEntity()
	{
		Movie movie = new Movie();
		movie.setTitle(title);
		movie.setDurationMin(durationMin);
		movie.setRating(rating);
		movie.setSynopsis(synopsis);
		movie.setDirector(director);
		movie.setActors(actors);
		movie.setGenre(genre);
		movie.setReleaseDate(releaseDate);
		movie.setIsActive(isActive); // 可能是 null，交給 Service 判斷
		return movie;
	}
}
