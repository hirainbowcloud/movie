package com.example.movie.dto;

import com.example.movie.model.Member;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@JsonPropertyOrder
({ "memberId","username", "name", "phone", "email" })  
@Data
public class MemberFindAllResponse
{
		private Integer memberId;
		private  String username;
		private  String name;
		private  String phone;
		private  String email;
		
		public static MemberFindAllResponse from (Member member)
		{
			MemberFindAllResponse res = new MemberFindAllResponse();
			res.setMemberId(member.getMemberId());
			res.setUsername(member.getUsername());
			res.setName(member.getName());
			res.setPhone(member.getPhone());
			res.setEmail(member.getEmail());
			return res;
		}
}
	
