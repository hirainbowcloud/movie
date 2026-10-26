package com.example.movie.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(name="movie_image")
@Data
@JsonPropertyOrder({"imageId","movieId","imageUrl","imageType", "sortOrder", "createdAt" }) 
public class MovieImage {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer imageId;
	
	//所屬電影（外鍵 → movie.movie_id）
	//保留 @Data，在兩邊的關聯欄位上加排除
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@JsonIgnore //轉 JSON 時不輸出，避免無限遞迴
	@NotNull(message="請指定電影")
	@ManyToOne(fetch = FetchType.LAZY)    
    @JoinColumn(name = "movie_id",nullable = false) 
	private Movie movie;	
	
	//唯讀的外鍵數字，方便 JSON 輸出時看到 movieId
	//真正寫入外鍵的是上面的 movie 欄位
	@Column(name = "movie_id", insertable = false, updatable = false)
	private Integer movieId;
	
	//圖片路徑，例如 /images/movies/1/poster.jpg
	@NotBlank(message = "圖片路徑不可空白")
	@Size(max = 500)
	@Column(nullable = false, length = 500)
	private String imageUrl;
	
	//POSTER=海報、STILL=劇照、BANNER=橫幅
	@NotBlank(message = "請選擇圖片類型")
	@Pattern(regexp = "POSTER|STILL|BANNER", message = "圖片類型只能是 POSTER、STILL、BANNER")
	@Column(nullable = false, length = 20)
	private String imageType;
	
	//顯示順序，小的在前
	@Column(nullable = false)
	private Integer sortOrder = 0;
	
	//上傳時間（由 MySQL 自動填入）
	@Column(insertable = false, updatable = false)
	private LocalDateTime createdAt;
}
