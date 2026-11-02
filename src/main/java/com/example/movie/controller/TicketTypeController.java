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

import com.example.movie.dao.TicketTypeService;
import com.example.movie.dto.PageResponse;
import com.example.movie.dto.TicketTypeRequest;
import com.example.movie.dto.TicketTypeResponse;
import com.example.movie.model.TicketType;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/ticket-type")
public class TicketTypeController {

	@Autowired
	TicketTypeService ticketTypeSrv;

	// GET /api/ticket-type?page=1&size=10 → 全部票種（分頁）
	@GetMapping
	public ResponseEntity<PageResponse<TicketTypeResponse>> list
	(@RequestParam(defaultValue = "1") int page,
	 @RequestParam(defaultValue = "10") int size)
	{
		Page<TicketTypeResponse> result = ticketTypeSrv.findAll(page, size)
				.map(TicketTypeResponse::from);
		if (result.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(PageResponse.from(result));
	}

	// GET /api/ticket-type/1 → 單一票種（找不到 → 404）
	@GetMapping("/{id}")
	public ResponseEntity<TicketTypeResponse> findById(@PathVariable Integer id)
	{
		TicketType t = ticketTypeSrv.findById(id);
		return ResponseEntity.ok(TicketTypeResponse.from(t));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<TicketTypeResponse> delete(@PathVariable Integer id)
	{
		return ticketTypeSrv.delete(id)
				.map(TicketTypeResponse::from)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@PostMapping
	public ResponseEntity<TicketTypeResponse> create(@Valid @RequestBody TicketTypeRequest req)
	{
		TicketType saved = ticketTypeSrv.create(req.toEntity());
		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(TicketTypeResponse.from(saved));
	}

	@PutMapping("/{id}")
	public ResponseEntity<TicketTypeResponse> update
	(@PathVariable Integer id, @Valid @RequestBody TicketTypeRequest req)
	{
		TicketType updated = ticketTypeSrv.update(id, req.toEntity());
		return ResponseEntity.ok(TicketTypeResponse.from(updated));
	}
}
