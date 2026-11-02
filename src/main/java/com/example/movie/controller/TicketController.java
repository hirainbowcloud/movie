package com.example.movie.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.movie.dao.TicketService;
import com.example.movie.dto.TicketResponse;
import com.example.movie.model.Ticket;

// 票只能透過 POST /api/orders 購票時新增，這裡只提供查詢
@RestController
@RequestMapping("/api/ticket")
public class TicketController {

	@Autowired
	TicketService ticketSrv;

	// GET /api/ticket?orderId=1     → 某筆訂單的票
	// GET /api/ticket?showtimeId=1  → 某個場次已售出的座位（畫座位圖用）
	@GetMapping
	public ResponseEntity<List<TicketResponse>> list
	(@RequestParam(required = false) Integer orderId,
			@RequestParam(required = false) Integer showtimeId)
	{
		List<Ticket> data;
		if (orderId != null) {
			data = ticketSrv.findByOrderId(orderId);
		} else if (showtimeId != null) {
			data = ticketSrv.findByShowtimeId(showtimeId);
		} else {
			throw new IllegalArgumentException("請指定 orderId 或 showtimeId"); // → 400
		}

		// 沒有票回傳空陣列 []：場次還沒賣出任何座位是正常情況
		List<TicketResponse> result = data.stream()
				.map(TicketResponse::from)
				.toList();
		return ResponseEntity.ok(result);
	}

	// GET /api/ticket/1 → 單一張票（找不到 → 404）
	@GetMapping("/{id}")
	public ResponseEntity<TicketResponse> findById(@PathVariable Integer id)
	{
		Ticket t = ticketSrv.findById(id);
		return ResponseEntity.ok(TicketResponse.from(t));
	}
}
