package com.example.movie.dto;

import com.example.movie.model.MovieImage;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// 新增 / 修改電影時，每張圖片要帶的資料（檔案本身另外用 files 上傳）
@Data
public class MovieImageRequest {

	// poster=海報、still=劇照、banner=橫幅
	@NotBlank(message = "請選擇圖片類型")
	private String imageType;

	//MovieImage是Entity
	public MovieImage toEntity()
	{
		MovieImage imgInfo = new MovieImage();
		imgInfo.setImageType(imageType);
		return imgInfo;
	}
}
