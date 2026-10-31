package com.example.movie.dto;

import com.example.movie.model.Hall;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

// 新增 / 修改影廳時，前端送來的資料（不含 hallId）
@Data
public class HallRequest {

	@NotBlank(message = "請輸入廳名")
	@Size(max = 20, message = "廳名最多 20 字")
	private String hallName;

	@NotNull(message = "請輸入排數")
	@Min(value = 10, message = "排數至少 10 排")
	private Integer rowCount;

	@NotNull(message = "請輸入第一排座位數")
	@Min(value = 1, message = "第一排座位數至少 1 個")
	private Integer firstRowSeats;

	public Hall toEntity()
	{
		Hall hall = new Hall();
		hall.setHallName(hallName);
		hall.setRowCount(rowCount);
		hall.setFirstRowSeats(firstRowSeats);
		return hall;
	}
}
