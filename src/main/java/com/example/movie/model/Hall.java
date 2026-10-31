package com.example.movie.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name="hall")
@Data
public class Hall 
{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="hall_id")
	private Integer hallId;

	//廳名
	@Column(name="hall_name", nullable = false, length = 20)
	private String hallName;

	//排數（預設 10 排）
	@Column(name="row_count", nullable = false)
	private Integer rowCount = 10;

	//第一排座位數（預設 8 個）
	@Column(name="first_row_seats", nullable = false)
	private Integer firstRowSeats = 8;
}