package com.example.movie.model;

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
@Table(name = "ticket")
@Data
public class Ticket {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ticket_id")
	private Integer ticketId;

	// 外鍵 → orders（寫入用）
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id", nullable = false)
	private Orders order;

	@Column(name = "order_id", insertable = false, updatable = false)
	private Integer orderId;

	// 外鍵 → showtime（寫入用）
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "showtime_id", nullable = false)
	private ShowTime showtime;

	@Column(name = "showtime_id", insertable = false, updatable = false)
	private Integer showtimeId;

	// 第幾排
	@Column(name = "row_no", nullable = false)
	private Integer rowNo;

	// 第幾號
	@Column(name = "seat_no", nullable = false)
	private Integer seatNo;

	// 外鍵 → ticket_type（寫入用）
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "type_id", nullable = false)
	private TicketType ticketType;

	@Column(name = "type_id", insertable = false, updatable = false)
	private Integer typeId;

	// 原價（打折前）
	@Column(name = "original_price", nullable = false)
	private Integer originalPrice;

	// 實際售價
	@Column(name = "price", nullable = false)
	private Integer price;
}
