package com.example.movie.util;

import com.example.movie.model.Hall;

// 座位規則：資料表只存「排數」和「第一排座位數」，每排幾個座位由這裡計算
// 目前規則和前端 seatUtils.js 相同：第一排 firstRowSeats 個，之後每往後一排多 1 個
// 例：firstRowSeats = 8 → 第 1 排 8 個、第 2 排 9 個、第 3 排 10 個…
// 規則確定要改時，只要改 seatsInRow() 這一個方法
public final class SeatRule {

	private SeatRule() {} //外面不能new

	// 第 rowNo 排有幾個座位(每多1排位子數量+1)
	// 因為每往後一排多 1 個，所以「第幾排減 1」就是「要多幾個」。
	// 第一排座位數 + 比第一排多的數量，回傳「這排座位數」	
	//int rowNo：要查第幾排
	// 第一廰的第5排有幾個座位?-> SeatsInRow(hall,5), retunr 12
	public static int seatsInRow(Hall hall, int rowNo)
	{		
		return hall.getFirstRowSeats() + (rowNo - 1); // 每排會增加一個坐位
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
