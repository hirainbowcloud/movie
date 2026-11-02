package com.example.movie.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
	
	@NotBlank(message = "請輸入帳號")
	private String username;
	
	@NotBlank(message = "請輸入密碼")
	private String password;

}
