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

import com.example.movie.dao.AdminService;
import com.example.movie.dto.AdminRequest;
import com.example.movie.dto.AdminResponse;
import com.example.movie.dto.PageResponse;
import com.example.movie.model.Admin;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

	@Autowired
	AdminService adminSrv;	

	@GetMapping
	public ResponseEntity<?> list
	(@RequestParam(required = false) String username,
			@RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "10") int size) 
	{
		if (username == null) {
			Page<AdminResponse> result = adminSrv.findAll(page, size)
					.map(AdminResponse::from);
			if (result.isEmpty()) {
				return ResponseEntity.notFound().build();
			}
			return ResponseEntity.ok(PageResponse.from(result));
		}
		return adminSrv.findByUsername(username)
				.map(AdminResponse::from)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@GetMapping("/search")
	public ResponseEntity<PageResponse<AdminResponse>> search
	(@RequestParam(required = false) String keyword,
			@RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "10") int size) 
	{
		//http://localhost:8080/api/admin/search?keyword=&page=1&size=10
		Page<AdminResponse> result = adminSrv.searchByKeyword(keyword, page, size)
				.map(AdminResponse::from);
		if (result.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(PageResponse.from(result));
	}	

	@DeleteMapping("/{username}")
	public ResponseEntity<AdminResponse> delete(@PathVariable String username) 
	{
		return adminSrv.delete(username)
				.map(AdminResponse::from)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@PostMapping
	public ResponseEntity<AdminResponse> create(@Valid @RequestBody AdminRequest req) 
	{
		Admin saved = adminSrv.create(req.toEntity());
		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(AdminResponse.from(saved));
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<AdminResponse> update
	(@PathVariable int id, @RequestBody Admin admin) 
	{
	    Admin updated = adminSrv.update(id, admin);
	    return ResponseEntity.ok(AdminResponse.from(updated));
	}
	
}
