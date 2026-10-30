package com.example.movie.dto;

import lombok.Data;

@Data
public class MemberUpdateRequest {

	private String username;
	private String password;
	private String name;
	private String phone;
	private String email;
	
}
