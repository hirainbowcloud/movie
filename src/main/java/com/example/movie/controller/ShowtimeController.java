package com.example.movie.controller;

import java.util.List;

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
import com.example.movie.dao.TicketService;
import com.example.movie.dto.PageResponse;
import com.example.movie.dto.SeatMapResponse;
import com.example.movie.dto.ShowtimeRequest;
import com.example.movie.dto.ShowtimeResponse;
import com.example.movie.model.ShowTime;
import com.example.movie.model.Ticket;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/showtime")
public class ShowtimeController {

	@Autowired
	ShowtimeService showtimeSrv;

	@Autowired
	TicketService ticketSrv;   // 座位圖用：查這個場次已售出的票

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
	
	// GET /api/showtime/upcoming?movieId=1 → 今天以後的場次（購票人用）電影介紹與場次頁
	@GetMapping("/upcoming")
	public ResponseEntity<List<ShowtimeResponse>> upcoming(@RequestParam Integer movieId)
	{
		List<ShowtimeResponse> result = showtimeSrv.findUpcomingByMovieId(movieId)
				.stream()
				.map(ShowtimeResponse::from)
				.toList();
		return ResponseEntity.ok(result);
	}

	// GET /api/showtime/1/seats → 座位圖：場次、影廳，以及每個座位的狀態（SOLD 已售出 / AVAILABLE 未售出）
	// 場次不存在 → 404
	@GetMapping("/{id}/seats")
	public ResponseEntity<SeatMapResponse> seats(@PathVariable Integer id)
	{
		ShowTime s = showtimeSrv.findById(id);                // 找不到會丟 ShowtimeNotFoundException → 404
		List<Ticket> sold = ticketSrv.findByShowtimeId(id);   // 這個場次已售出的票，沒有就是空清單
		return ResponseEntity.ok(SeatMapResponse.from(s, sold));
	}
}
