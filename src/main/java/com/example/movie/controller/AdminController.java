package com.example.movie.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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
import com.example.movie.dto.AdminCreateRequest;
import com.example.movie.dto.AdminResponse;
import com.example.movie.model.Admin;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

	@Autowired
	AdminService adminSrv;	

	@GetMapping("/page")
	public ResponseEntity<String> page(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		return ResponseEntity.ok("page=" + page + ", size=" + size);
	}
	
	@GetMapping("/list")
	public ResponseEntity<?> list(@RequestParam(required = false) String username) {
		if (username == null) {
			List<AdminResponse> result = adminSrv.findAll()
					.stream()
					.map(AdminResponse::from)
					.toList();
			if (result.isEmpty()) {
				return ResponseEntity.notFound().build();
			}
			return ResponseEntity.ok(result);
		}
		return adminSrv.findByUsername(username)
				.map(AdminResponse::from)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}


	@GetMapping("/search")
	public ResponseEntity<List<AdminResponse>> search(@RequestParam(required = false) String keyword) {
		//http://localhost:8080/api/admin/search?keyword=
		List<AdminResponse> result = adminSrv.searchByKeyword(keyword)
				.stream()
				.map(AdminResponse::from)
				.toList();
		if (result.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(result);
	}	

	@DeleteMapping("/{username}")
	public ResponseEntity<AdminResponse> delete(@PathVariable String username) {
		return adminSrv.delete(username)
				.map(AdminResponse::from)
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}

	@PostMapping
	public ResponseEntity<AdminResponse> create(AdminCreateRequest req) {
		Admin saved = adminSrv.create(req.toEntity());
		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(AdminResponse.from(saved));
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<AdminResponse> update(@PathVariable int id, @RequestBody Admin admin) {
	    Admin updated = adminSrv.update(id, admin);
	    return ResponseEntity.ok(AdminResponse.from(updated));
	}
}
