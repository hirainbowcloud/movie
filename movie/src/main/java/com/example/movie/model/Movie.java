package com.example.movie.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(name = "movie")
@Data
@JsonPropertyOrder
({ "movieId","title", "durationMin", "rating", "synopsis",
	"director", "actors","genre", "releaseDate", "trailerUrl", 
	"isActive", "updatedAt","images" })  
public class Movie {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer movieId;

	@NotBlank(message = "片名不可以空白")
	@Size(max = 100, message = "片名最多100字")
	@Column(nullable = false)
	private String title;

	@NotNull(message = "請輸入片長")
	@Min(value = 1, message = "片長至少1分鐘")
	@Column(nullable = false)
	private Integer durationMin;

	// 分級:普遍級/保護級
	@NotBlank(message = "請選擇分級")
	@Column(nullable = false)
	private String rating;

	// 電影簡介
	@Column(columnDefinition = "TEXT")
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
	@Column(nullable = false)
	private LocalDate releaseDate;

	// 預告片網址
	@Size(max = 500)
	private String trailerUrl;

	// 上下架
	@Column(columnDefinition = "TINYINT(1)")
	private Boolean isActive = true;

	// 最後修改
	@Column(insertable = false, updatable = false)
	private LocalDateTime updatedAt;

	// 圖片（一部電影有多張圖）
	// 保留 @Data，在兩邊的關聯欄位上加排除
	// mappedBy = "movie" → 對應 MovieImage 裡的 movie 欄位
	// cascade（連鎖操作）
	// orphanRemoval（MovieImage裡的圖片只要不在清單裡，JPA 會自動把那筆資料從 DB 刪掉）
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("sortOrder ASC")
	private List<MovieImage> images = new ArrayList<>();

	// 新增圖片：兩邊一起設定，外鍵才會正確存進 DB
	public void addImage(MovieImage image) {
		images.add(image);
		image.setMovie(this);
	}

	// 移除圖片
	public void removeImage(MovieImage image) {
		images.remove(image);
		image.setMovie(null);
	}

}
