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

import com.example.movie.dao.HallService;
import com.example.movie.dto.HallRequest;
import com.example.movie.dto.HallResponse;
import com.example.movie.dto.PageResponse;
import com.example.movie.model.Hall;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/hall")
public class HallController {

	@Autowired
	HallService hallSrv;

	// GET /api/hall?page=1&size=10        → 全部影廳（分頁）
	// GET /api/hall?hallName=A廳           → 依廳名查單筆
	@GetMapping
	public ResponseEntity<?> list
	(@RequestParam(required = false) String hallName,
			@RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "10") int size) 
	{
		if (hallName == null) {
			Page<HallResponse> result = hallSrv.findAll(page, size)
					.map(HallResponse::from);
			if (result.isEmpty()) {
				return ResponseEntity.notFound().build();
			}
			return ResponseEntity.ok(PageResponse.from(result));
		}
		return hallSrv.findByHallName(hallName)
				.map(HallResponse::from)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	// GET /api/hall/1 → 單一影廳（找不到 → 404）
	@GetMapping("/{id}")
	public ResponseEntity<HallResponse> findById(@PathVariable Integer id) 
	{
		Hall hall = hallSrv.findById(id);
		return ResponseEntity.ok(HallResponse.from(hall));
	}

	@GetMapping("/search")
	public ResponseEntity<PageResponse<HallResponse>> search
	(@RequestParam(required = false) String keyword,
			@RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "10") int size) 
	{
		//http://localhost:8080/api/hall/search?keyword=&page=1&size=10
		Page<HallResponse> result = hallSrv.searchByKeyword(keyword, page, size)
				.map(HallResponse::from);
		if (result.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(PageResponse.from(result));
	}	

	@DeleteMapping("/{id}")
	public ResponseEntity<HallResponse> delete(@PathVariable Integer id) 
	{
		return hallSrv.delete(id)
				.map(HallResponse::from)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@PostMapping
	public ResponseEntity<HallResponse> create(@Valid @RequestBody HallRequest req) 
	{
		Hall saved = hallSrv.create(req.toEntity());
		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(HallResponse.from(saved));
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<HallResponse> update
	(@PathVariable Integer id, @Valid @RequestBody HallRequest req) 
	{
	    Hall updated = hallSrv.update(id, req.toEntity());
	    return ResponseEntity.ok(HallResponse.from(updated));
	}
	
}
