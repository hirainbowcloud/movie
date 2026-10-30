package com.example.movie.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.movie.Repository.MemberRepository;
import com.example.movie.dto.MemberUpdateRequest;
import com.example.movie.model.Member;
import com.example.movie.util.PasswordUtil;

@Service
public class MemberService {

	@Autowired
	private MemberRepository rep;
	
	public List<Member> searchByKeyWord(String keyword)
	{
		if(keyword == null || keyword.isBlank())
		{
			return rep.findAll();
		}
		return rep.searchByKeyword(keyword);
	}
	
	public Optional<Member> findByUserName(String username)
	{
		return rep.findByUsername(username);
	}
	
	public List<Member> findAll()
	{
		return rep.findAll();
	}
	
	public Optional<Member> update(Integer id, MemberUpdateRequest req)
	{
	    return rep.findById(id).map(m -> {
	        m.setName(req.getName());
	        m.setPhone(req.getPhone());
	        m.setEmail(req.getEmail());
	        
	        if(req.getPassword() != null && !req.getPassword().isBlank())
	        {
	        	m.setPassword(PasswordUtil.sha256(req.getPassword()));
	        }
	        return rep.save(m);
	    });
	}
}
