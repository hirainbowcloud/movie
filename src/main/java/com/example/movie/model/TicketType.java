package com.example.movie.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "ticket_type")
@Data
public class TicketType {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "type_id")
	private Integer typeId;

	// 票種名稱
	@Column(name = "type_name", nullable = false, length = 30)
	private String typeName;

	// 售價（打折後的價格）
	@Column(name = "base_price", nullable = false)
	private Integer basePrice;

	// 折扣率 DECIMAL(3,2)：0.50 = 5折、1.00 = 不打折
	// 金額、比率用 BigDecimal，避免 double 的小數誤差
	@Column(name = "discount_rate", nullable = false, precision = 3, scale = 2)
	private BigDecimal discountRate;

	// 說明
	@Column(name = "description", length = 100)
	private String description;
}
