package com.example.movie.dto;

import com.example.movie.model.Admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminResponse {

	private Integer adminId;
	
	@NotBlank(message = "使用者名稱不得為空值")
	private String username;	
	
	@NotBlank(message = "職稱不得為空值")
	private String name;
	
	public static AdminResponse from(Admin admin)
	{
		AdminResponse res = new AdminResponse();
		res.setAdminId(admin.getAdminId());
		res.setUsername(admin.getUsername());
		res.setName(admin.getName());
		return res;
	}
}
