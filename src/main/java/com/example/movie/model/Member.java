package com.example.movie.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "member")
@Data
public class Member {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "member_id")
	private Integer memberId;

	@Column(name = "username", nullable = false, unique = true, length = 30)
	private String username;

	@Column(name = "password", nullable = false, length = 64, columnDefinition = "CHAR(64)")
	private String password;

	@Column(name = "name", nullable = false, length = 50)
	private String name;

	@Column(name = "phone", length = 20)
	private String phone;

	@Column(name = "email", length = 100)
	private String email;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;
}
