package com.example.movie.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.movie.Repository.MemberRepository;
import com.example.movie.exception.MemberNotFoundException;
import com.example.movie.exception.UsernameAlreadyExistsException;
import com.example.movie.model.Member;
import com.example.movie.util.PasswordUtil;

@Service
public class MemberService {

	@Autowired
	private MemberRepository rep;
	
	public Page<Member> searchByKeyWord(String keyword, int page, int size)
	{		 
		return rep.searchByKeyword(keyword, buildPageable(page, size));
	}
	
	public Page<Member> findAll(int page, int size)
	{
		return rep.findAll(buildPageable(page, size));
	}
	
	// 檢查頁碼、每頁筆數，並依 memberId 排序
	private Pageable buildPageable(int page, int size)
	{
		if (page < 1) page = 1;
		if (size < 1 || size > 100) size = 10;   // 防止前端一次要太多筆
		return PageRequest.of(page - 1, size, Sort.by("memberId").ascending());
	}
	
	public Optional<Member> findByUserName(String username)
	{
		return rep.findByUsername(username);
	}
	
	
	public Optional<Member> update(Integer id, Member req)
	{
	    return rep.findById(id).map(m -> {
	        m.setName(req.getName());
	        m.setPhone(req.getPhone());
	        m.setEmail(req.getEmail());
	        // 有傳新密碼才更新，沒傳就保留原本的密碼
	        if (req.getPassword() != null && !req.getPassword().isBlank())
	        {
	            m.setPassword(PasswordUtil.sha256(req.getPassword()));
	        }
	        return rep.save(m);
	    });
	}
	
	public Member create(Member member)
	{
		if(rep.existsByUsername(member.getUsername()))
		{
			throw new UsernameAlreadyExistsException(member.getUsername());
		}
		member.setPassword(PasswordUtil.sha256(member.getPassword()));
		return rep.save(member);
	}
	
	public Member delete(Integer id)
	{
		Member member = rep.findById(id)
				.orElseThrow(()-> new MemberNotFoundException(id));
		rep.delete(member);
		return member;
	}
}
