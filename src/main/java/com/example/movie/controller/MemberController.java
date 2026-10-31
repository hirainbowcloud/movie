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

import com.example.movie.dao.MemberService;
import com.example.movie.dto.MemberResponse;
import com.example.movie.dto.MemberFindAllResponse;
import com.example.movie.dto.MemberRequest;
import com.example.movie.dto.MemberUpdateResponse;
import com.example.movie.dto.PageResponse;
import com.example.movie.model.Member;
import com.example.movie.util.PasswordUtil;

@RestController
@RequestMapping("/api/member")
public class MemberController {
	@Autowired
	MemberService srv;

	@GetMapping
	public ResponseEntity<PageResponse<MemberFindAllResponse>> findAll
	(
			@RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "10") int size) 
	{
		Page<MemberFindAllResponse> result = srv.findAll(page, size)
				.map(MemberFindAllResponse::from);
		return ResponseEntity.ok(PageResponse.from(result));
	}
	
	@GetMapping("/search")
	public ResponseEntity<PageResponse<MemberFindAllResponse>> searchByKeyword
	(
	        @RequestParam String keyword,
	        @RequestParam(defaultValue = "1") int page,
	        @RequestParam(defaultValue = "10") int size)
	{
		//http://localhost:8080/api/member?keyword=&page&size=
	    Page<MemberFindAllResponse> result = srv.searchByKeyWord(keyword, page, size)
	            .map(MemberFindAllResponse::from);
	    
	    if(result.isEmpty())
	    {
	    	return ResponseEntity.notFound().build();
	    }

	    return ResponseEntity.ok(PageResponse.from(result));
	}
	

	@PutMapping("/{id}")
	public ResponseEntity<MemberUpdateResponse> update
	(@PathVariable Integer id, @RequestBody MemberRequest req) 
	{

		boolean passwordChanged = req.getPassword() != null && !req.getPassword().isBlank();
		return srv.update(id, req.toEntity())
				.map(res -> MemberUpdateResponse.from(res, passwordChanged))
				.map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
	}
	
	@PostMapping 
	public ResponseEntity<MemberResponse> create
	(@RequestBody MemberRequest req) 
	{		
		Member saved = srv.create(req.toEntity());		
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(MemberResponse.from(saved));
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<MemberResponse> delete 
	(@PathVariable("id") Integer memberId)
	{
		Member memberObj = srv.delete(memberId);			
		MemberResponse res = MemberResponse.from(memberObj);
		return ResponseEntity.ok(res);
	}	 
}
