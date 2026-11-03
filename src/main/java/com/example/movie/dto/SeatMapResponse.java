package com.example.movie.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.example.movie.model.Hall;
import com.example.movie.model.ShowTime;
import com.example.movie.model.Ticket;
import com.example.movie.util.SeatRule;

// 座位圖：哪個場次、哪一廳，以及每個座位是否已售出
// 座位狀態不存在資料庫：有這個場次、這個座位的 ticket 就是已售出，沒有就是未售出
public record SeatMapResponse(
		Integer showtimeId,
		Integer movieId,
		String movieTitle,
		LocalDate showDate,
		LocalTime startTime,
		Integer hallId,
		String hallName,
		Integer rowCount,
		Integer firstRowSeats,
		int totalSeats,
		int soldCount,
		int availableCount,
		List<SeatStatusResponse> seats)
{
	// s：場次；soldTickets：這個場次已售出的票
	public static SeatMapResponse from(ShowTime s, List<Ticket> soldTickets)
	{
		Hall hall = s.getHall();

		// 已售出的座位，例如 "5-3" 代表第 5 排第 3 號
		Set<String> soldKeys = new HashSet<>();
		for (Ticket t : soldTickets)
		{
			soldKeys.add(t.getRowNo() + "-" + t.getSeatNo());
		}

		// 依影廳大小列出每一個座位，有在 soldKeys 裡就是已售出
		List<SeatStatusResponse> seats = new ArrayList<>();
		int soldCount = 0;
		for (int row = 1; row <= hall.getRowCount(); row++)
		{
			for (int seat = 1; seat <= SeatRule.seatsInRow(hall, row); seat++)
			{
				boolean sold = soldKeys.contains(row + "-" + seat);
				if (sold)
				{
					soldCount++;
				}
				seats.add(new SeatStatusResponse(row, seat,
						sold ? SeatStatusResponse.SOLD : SeatStatusResponse.AVAILABLE));
			}
		}

		return new SeatMapResponse(
				s.getShowtimeId(),
				s.getMovie().getMovieId(),
				s.getMovie().getTitle(),
				s.getShowDate(),
				s.getStartTime(),
				hall.getHallId(),
				hall.getHallName(),
				hall.getRowCount(),
				hall.getFirstRowSeats(),
				seats.size(),
				soldCount,
				seats.size() - soldCount,
				seats);
	}
}
