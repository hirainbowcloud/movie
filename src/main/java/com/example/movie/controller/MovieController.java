package com.example.movie.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.movie.dao.MovieService;
import com.example.movie.dto.MovieImageRequest;
import com.example.movie.dto.MovieRequest;
import com.example.movie.dto.MovieResponse;
import com.example.movie.dto.PageResponse;
import com.example.movie.model.Movie;
import com.example.movie.model.MovieImage;

import jakarta.validation.Valid;

// Controller 負責 DTO ↔ model 轉換；Service 只處理 model
// 錯誤由 GlobalExceptionHandler 統一處理：
//   MovieNotFoundException → 404、IllegalArgumentException / @Valid 失敗 → 400
@RestController
@RequestMapping("/api/movie")
public class MovieController {

	@Autowired
	MovieService srv;

	// 查詢上架中的電影（分頁）：GET /api/movie?page=1&size=10
	@GetMapping
	public ResponseEntity<PageResponse<MovieResponse>> getIsActive
	(@RequestParam(defaultValue = "1") int page,
	 @RequestParam(defaultValue = "10") int size)
	{
		Page<MovieResponse> result = srv.findIsActive(page, size)
				.map(MovieResponse::from);
		if (result.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(PageResponse.from(result));
	}

	// 查詢下架的電影（分頁）：GET /api/movie/inactive?page=1&size=10
	@GetMapping("/inactive")
	public ResponseEntity<PageResponse<MovieResponse>> getIsInactive
	(@RequestParam(defaultValue = "1") int page,
	 @RequestParam(defaultValue = "10") int size)
	{
		Page<MovieResponse> result = srv.findIsInactive(page, size)
				.map(MovieResponse::from);
		if (result.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(PageResponse.from(result));
	}

	// 查詢單一電影：GET /api/movie/1（找不到 → 404）
	@GetMapping("/{id}")
	public ResponseEntity<MovieResponse> getMovieInfoByMovieId
	(@PathVariable int id) 
	{
		Movie movie = srv.findMovieByMovieId(id);
		return ResponseEntity.ok(MovieResponse.from(movie));
	}

	// 新增電影 + 圖片：POST /api/movie/add（multipart/form-data）
	@PostMapping(path = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<MovieResponse> createMovie
	(@Valid @RequestPart("movie") MovieRequest req, // 電影資料（JSON）
			@RequestPart(value = "images", required = false) List<MovieImageRequest> images, // 每張圖的類型（JSON 陣列），沒傳由 Service 回 400
			@RequestPart(value = "files", required = false) List<MultipartFile> files, // 圖片檔
			@RequestPart(value = "trailer", required = false) MultipartFile trailer) // 預告片
	{
		Movie movie = req.toEntity();
		List<MovieImage> imgs = toImageEntities(images);

		Movie saved = srv.createMovie(movie, imgs, files, trailer);
		return ResponseEntity.status(HttpStatus.CREATED).body(MovieResponse.from(saved)); // 201 新增成功
	}

	// 修改電影：PUT /api/movie/1（multipart/form-data）
	// 不換圖：images、files 都不要傳；不換預告片：trailer 不要傳
	@PutMapping(path = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<MovieResponse> updateMovie
	(@PathVariable int id,
	 @Valid @RequestPart("movie") MovieRequest req, // 電影資料（JSON）
	 @RequestPart(value = "images", required = false) List<MovieImageRequest> imagList, // 每張圖的類型（JSON 陣列）
	 @RequestPart(value = "files", required = false) List<MultipartFile> files, // 圖片檔
	 @RequestPart(value = "trailer", required = false) MultipartFile trailer) // 預告片
	{
		Movie form = req.toEntity();
		List<MovieImage> imgsType = toImageEntities(imagList);

		Movie updated = srv.updateMovie(id, form, imgsType, files, trailer);
		return ResponseEntity.ok(MovieResponse.from(updated)); // 200 修改成功
	}

	// MovieImageRequest裡只有String imageType的轉換
	private List<MovieImage> toImageEntities(List<MovieImageRequest> imagesInfo)
	{
		if (imagesInfo == null) {
			return null; //圖片資料（沒傳就回傳 null）
		}
		return imagesInfo.stream()
				.map(MovieImageRequest::toEntity) //DTO → model
				.toList();
	}

}
