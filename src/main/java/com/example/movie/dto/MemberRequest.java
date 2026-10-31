package com.example.movie.dto;

import com.example.movie.model.Member;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MemberRequest {

	private String username;
	
	@NotBlank(message = "密碼不得為空值")
	private String password;
	
	@NotBlank(message = "姓名不得為空值")
	private String name;
	
	@NotBlank(message = "電話不得為空值")
	private String phone;
	
	@NotBlank(message = "email不得為空值")
	private String email;
	
	public Member toEntity()
	{
		Member m = new Member();
		m.setUsername(this.username);
	    m.setName(this.name);
	    m.setPhone(this.phone);
	    m.setEmail(this.email);
	    m.setPassword(this.password);
	    return m;
	}
	
}
