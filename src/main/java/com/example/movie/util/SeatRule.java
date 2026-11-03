package com.example.movie.util;

import com.example.movie.model.Hall;

// 座位規則：資料表只存「排數」和「第一排座位數」，每排幾個座位由這裡計算
// 目前規則和前端 seatUtils.js 相同：第一排 firstRowSeats 個，之後每往後一排多 1 個
// 例：firstRowSeats = 8 → 第 1 排 8 個、第 2 排 9 個、第 3 排 10 個…
// 規則確定要改時，只要改 seatsInRow() 這一個方法
public final class SeatRule {

	private SeatRule() {}

	// 第 rowNo 排有幾個座位
	public static int seatsInRow(Hall hall, int rowNo)
	{
		return hall.getFirstRowSeats() + (rowNo - 1);
	}

	// 整個影廳的座位總數
	public static int totalSeats(Hall hall)
	{
		int total = 0;
		for (int row = 1; row <= hall.getRowCount(); row++)
		{
			total += seatsInRow(hall, row);
		}
		return total;
	}
}
