package com.example.movie.dto;

import com.example.movie.model.Admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminCreateRequest {	
	
	@NotBlank(message = "使用者名稱不得為空值")
	private String username;

	@NotBlank(message = "密碼不得為空值")
	private String password;

	@NotBlank(message = "職稱不得為空值")
	private String name;
	
	public Admin toEntity()
	{
		Admin admin = new Admin();
		admin.setUsername(username);
		admin.setPassword(password);
		admin.setName(name);
		return admin;
	}
}
