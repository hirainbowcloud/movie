package com.example.movie.model;

import java.time.LocalDate;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(name = "showtime")
@Data
public class ShowTime {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "showtime_id")
	private Integer showtimeId;

	// 外鍵 → movie（寫入用）
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "movie_id", nullable = false)
	private Movie movie;
	
	@Column(name = "movie_id", insertable = false, updatable = false)
	private Integer movieId;

	// 外鍵 → hall（寫入用）
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "hall_id", nullable = false)
	private Hall hall;

	@Column(name = "hall_id", insertable = false, updatable = false)
	private Integer hallId;

	// 放映日期
	@Column(name = "show_date", nullable = false)
	private LocalDate showDate;

	// 開演時間
	@Column(name = "start_time", nullable = false)
	private LocalTime startTime;
}
