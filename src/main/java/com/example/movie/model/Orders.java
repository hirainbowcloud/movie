package com.example.movie.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(name = "orders")
@Data
public class Orders {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "order_id")
	private Integer orderId;

	// 外鍵 → member（寫入用）
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	// 唯讀的 memberId
	@Column(name = "member_id", insertable = false, updatable = false)
	private Integer memberId;

	// 外鍵 → showtime（寫入用）
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "showtime_id", nullable = false)
	private ShowTime showtime;

	// 唯讀的 showtimeId
	@Column(name = "showtime_id", insertable = false, updatable = false)
	private Integer showtimeId;

	// 原價小計（所有票的 original_price 加總）
	@Column(name = "original_amount", nullable = false)
	private Integer originalAmount;

	// 折扣金額（原價小計 - 實收金額）
	@Column(name = "discount_amount", nullable = false)
	private Integer discountAmount;

	// 實收金額（所有票的 price 加總）
	@Column(name = "total_amount", nullable = false)
	private Integer totalAmount;

	// 下單時間
	@Column(name = "order_time", nullable = false)
	private LocalDateTime orderTime;

	// 一筆訂單有多張票
	// mappedBy = "order" → 對應 Ticket 裡的 order 欄位
	// cascade = ALL → 存訂單時，票一起存
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	@JsonIgnore
	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
	private List<Ticket> tickets = new ArrayList<>();

	// 新增票：兩邊一起設定，外鍵才會正確存進 DB
	public void addTicket(Ticket ticket) {
		tickets.add(ticket);
		ticket.setOrder(this);
	}
}
