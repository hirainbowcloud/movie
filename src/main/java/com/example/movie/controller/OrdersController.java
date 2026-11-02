package com.example.movie.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.movie.dao.OrdersService;
import com.example.movie.dto.OrdersRequest;
import com.example.movie.dto.OrdersResponse;
import com.example.movie.dto.PageResponse;
import com.example.movie.model.Orders;

import jakarta.validation.Valid;

// 訂單只提供：查詢、購票（新增）。不提供修改、刪除（ERD：管理者只能查詢訂單）
@RestController
@RequestMapping("/api/orders")
public class OrdersController {

	@Autowired
	OrdersService ordersSrv;

	// GET /api/orders?page=1&size=10              → 全部訂單（管理者）
	// GET /api/orders?memberId=1&page=1&size=10   → 某位會員的訂單（購票人）
	@GetMapping
	public ResponseEntity<PageResponse<OrdersResponse>> list
	(@RequestParam(required = false) Integer memberId,
			@RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "10") int size)
	{
		Page<Orders> data = (memberId == null)
				? ordersSrv.findAll(page, size)
				: ordersSrv.findByMemberId(memberId, page, size);

		Page<OrdersResponse> result = data.map(OrdersResponse::from);
		if (result.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(PageResponse.from(result));
	}

	// GET /api/orders/1 → 單一訂單 + 票（找不到 → 404）
	@GetMapping("/{id}")
	public ResponseEntity<OrdersResponse> findById(@PathVariable Integer id)
	{
		Orders o = ordersSrv.findById(id);
		return ResponseEntity.ok(OrdersResponse.from(o));
	}

	// POST /api/orders → 購票（一筆訂單 + 多張票）
	@PostMapping
	public ResponseEntity<OrdersResponse> create(@Valid @RequestBody OrdersRequest req)
	{
		Orders saved = ordersSrv.create(req.toEntity(), req.toTicketEntities());
		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(OrdersResponse.from(saved));
	}
}
