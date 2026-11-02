package com.example.movie.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.movie.dao.ShowtimeService;
import com.example.movie.dto.PageResponse;
import com.example.movie.dto.ShowtimeRequest;
import com.example.movie.dto.ShowtimeResponse;
import com.example.movie.model.ShowTime;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/showtime")
public class ShowtimeController {

	@Autowired
	ShowtimeService showtimeSrv;

	// GET /api/showtime?page=1&size=10            → 全部場次（分頁）
	// GET /api/showtime?movieId=1&page=1&size=10  → 某部電影的場次
	@GetMapping
	public ResponseEntity<PageResponse<ShowtimeResponse>> list
	(@RequestParam(required = false) Integer movieId,
			@RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "10") int size)
	{
		Page<ShowTime> data = (movieId == null)
				? showtimeSrv.findAll(page, size)
				: showtimeSrv.findByMovieId(movieId, page, size);

		Page<ShowtimeResponse> result = data.map(ShowtimeResponse::from);
		if (result.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(PageResponse.from(result));
	}

	// GET /api/showtime/1 → 單一場次（找不到 → 404）
	@GetMapping("/{id}")
	public ResponseEntity<ShowtimeResponse> findById(@PathVariable Integer id)
	{
		ShowTime s = showtimeSrv.findById(id);
		return ResponseEntity.ok(ShowtimeResponse.from(s));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<ShowtimeResponse> delete(@PathVariable Integer id)
	{
		return showtimeSrv.delete(id)
				.map(ShowtimeResponse::from)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@PostMapping
	public ResponseEntity<ShowtimeResponse> create(@Valid @RequestBody ShowtimeRequest req)
	{
		ShowTime saved = showtimeSrv.create(req.toEntity());
		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(ShowtimeResponse.from(saved));
	}

	@PutMapping("/{id}")
	public ResponseEntity<ShowtimeResponse> update
	(@PathVariable Integer id, @Valid @RequestBody ShowtimeRequest req)
	{
		ShowTime updated = showtimeSrv.update(id, req.toEntity());
		return ResponseEntity.ok(ShowtimeResponse.from(updated));
	}
}
