package com.example.movie.dto;

import java.math.BigDecimal;

import com.example.movie.model.TicketType;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

// 新增 / 修改票種時，前端送來的資料
@Data
public class TicketTypeRequest {

	private Integer typeId;

	@NotBlank(message = "請輸入票種名稱")
	@Size(max = 30, message = "票種名稱最多 30 字")
	private String typeName;

	@NotNull(message = "請輸入售價")
	@Min(value = 0, message = "售價不能是負數")
	private Integer basePrice;

	// 0.01 ~ 1.00，例如 0.80 = 8折
	@NotNull(message = "請輸入折扣率")
	@DecimalMin(value = "0.01", message = "折扣率至少 0.01")
	@DecimalMax(value = "1.00", message = "折扣率最多 1.00")
	private BigDecimal discountRate;

	@Size(max = 100, message = "說明最多 100 字")
	private String description;

	public TicketType toEntity()
	{
		TicketType t = new TicketType();
		t.setTypeId(typeId);
		t.setTypeName(typeName);
		t.setBasePrice(basePrice);
		t.setDiscountRate(discountRate);
		t.setDescription(description);
		return t;
	}
}
