package com.example.movie.dto;

import com.example.movie.model.Member;

public record MemberResponse(
	Integer memberId,
	String username,
	String name,
	String phone,
	String email

) 
{
	public static MemberResponse from(Member m)
	{
		return new MemberResponse(
				 m.getMemberId(),
	                m.getUsername(),
	                m.getName(),
	                m.getPhone(),
	                m.getEmail()
				);
	}	
}
