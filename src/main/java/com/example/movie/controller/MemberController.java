package com.example.movie.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.movie.dao.MemberService;
import com.example.movie.dto.MemberFindAllResponse;
import com.example.movie.dto.MemberUpdateRequest;
import com.example.movie.dto.MemberUpdateResponse;

@RestController
@RequestMapping("/api/member/")
public class MemberController {
	@Autowired
	MemberService srv;
	
	@GetMapping
	public ResponseEntity<List<MemberFindAllResponse>> findAll()
	{
		List<MemberFindAllResponse> result = srv.findAll()
				.stream()
				.map(MemberFindAllResponse::from)
				.toList();
		return ResponseEntity.ok(result);
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<MemberUpdateResponse> update (@PathVariable Integer id,
												  @RequestBody MemberUpdateRequest req)	{
		boolean passwordChanged = req.getPassword() != null && !req.getPassword().isBlank();
		
		return srv.update(id, req)
				.map(m -> MemberUpdateResponse.from(m,passwordChanged))
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}
	
	

}
