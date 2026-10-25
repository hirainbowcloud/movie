package com.example.movie.controller;

import java.io.UncheckedIOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.movie.dao.MovieService;
import com.example.movie.model.Info;
import com.example.movie.model.Movie;
import com.example.movie.model.MovieImage;

@RestController
@RequestMapping("api/movie")
public class MovieController {

	@Autowired
	MovieService srv;

	@GetMapping
	public ResponseEntity getIsActive() {
		Info info = srv.findIsActive();
		if (info.getMovies().size() > 0) {
			return ResponseEntity.ok(info);
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	@GetMapping("/{id}")
	public ResponseEntity<Info> getMovieInfoByMovieId(@PathVariable int id) {
		Info info = srv.findMovieByMovieId(id);

		if ("success".equals(info.getMessage())) {
			return ResponseEntity.ok(info);
		}
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(info); // 404
	}

	// 新增電影 + 圖片：POST /api/movie/add（multipart/form-data）
	@PostMapping(path = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<Info> createMovie(@RequestPart("movie") Movie movie, // 電影資料（JSON）
			@RequestPart("images") List<MovieImage> images, // 每張圖的類型（JSON 陣列）
			@RequestPart(value = "files", required = false) List<MultipartFile> files, // 圖片檔
			@RequestPart(value = "trailer", required = false) MultipartFile trailer) // 預告片
	{

		Info info = srv.saveImage(movie, images, files, trailer);

		if ("success".equals(info.getMessage())) {
			return ResponseEntity.status(HttpStatus.CREATED).body(info); // 201 新增成功
		}
		return ResponseEntity.badRequest().body(info); // 400 資料有誤
	}

	// 圖片寫入硬碟失敗（Service 丟出 UncheckedIOException）→ 500 + 訊息
	@ExceptionHandler(UncheckedIOException.class)
	public ResponseEntity<Info> handleIOError(UncheckedIOException e) 
	{
		Info info = new Info();
		info.setMessage("fail: " + e.getMessage());
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(info);
	}

}
