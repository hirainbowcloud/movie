package com.example.movie.dto;

import com.example.movie.model.Ticket;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

// 購票時，每一張票要帶的資料：座位 + 票種（價格由後端依票種計算，前端不能指定）
@Data
public class TicketItemRequest {

	@NotNull(message = "請選擇排數")
	@Min(value = 1, message = "排數至少是 1")
	private Integer rowNo;

	@NotNull(message = "請選擇座號")
	@Min(value = 1, message = "座號至少是 1")
	private Integer seatNo;

	@NotNull(message = "請選擇票種")
	private Integer typeId;

	public Ticket toEntity()
	{
		Ticket t = new Ticket();
		t.setRowNo(rowNo);
		t.setSeatNo(seatNo);
		t.setTypeId(typeId); // 先放在唯讀欄位，Service 會查出 TicketType 再設定關聯
		return t;
	}
}
