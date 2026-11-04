package com.example.movie.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.movie.Repository.TicketRepository;
import com.example.movie.exception.TicketNotFoundException;
import com.example.movie.model.Ticket;

// 票只能透過購票（OrdersService.create）新增，這裡只負責查詢
@Service
public class TicketService {

	@Autowired
	TicketRepository ticketRepository;

	@Transactional(readOnly = true)
	public Ticket findById(Integer id)
	{
		return ticketRepository.findById(id)
				.orElseThrow(() -> new TicketNotFoundException(id));
	}

	// 某筆訂單的票
	public List<Ticket> findByOrderId(Integer orderId)
	{
		return ticketRepository.findByOrderIdOrderByTicketId(orderId);
	}

	// showtime_id 是這個場次的資料。但它搜的是 ticket 表，所以結果就是「這個場次已售出的座位」（畫座位圖用）
	public List<Ticket> findByShowtimeId(Integer showtimeId)
	{
		return ticketRepository.findSoldSeats(showtimeId);
	}
}
