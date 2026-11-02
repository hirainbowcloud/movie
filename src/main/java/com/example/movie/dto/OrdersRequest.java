package com.example.movie.dto;

import java.util.List;

import com.example.movie.model.Orders;
import com.example.movie.model.Ticket;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

// 購票：一筆訂單 + 多張票
// 金額（原價小計、折扣、實收）由後端計算，前端不用傳
@Data
public class OrdersRequest {

	@NotNull(message = "請指定會員")
	private Integer memberId;

	@NotNull(message = "請選擇場次")
	private Integer showtimeId;

	// @Valid：清單裡的每一張票也要檢查
	@NotEmpty(message = "至少要選一個座位")
	@Valid
	private List<TicketItemRequest> tickets;

	public Orders toEntity()
	{
		Orders o = new Orders();
		o.setMemberId(memberId);       // 先放在唯讀欄位，Service 會查出 Member 再設定關聯
		o.setShowtimeId(showtimeId);
		return o;
	}

	public List<Ticket> toTicketEntities()
	{
		return tickets.stream()
				.map(TicketItemRequest::toEntity)
				.toList();
	}
}
