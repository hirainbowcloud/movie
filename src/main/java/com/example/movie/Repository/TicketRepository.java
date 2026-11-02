package com.example.movie.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.movie.model.Ticket;

@Repository
public interface TicketRepository extends JpaRepository<Ticket,Integer> {

	// 某筆訂單的票
	List<Ticket> findByOrderIdOrderByTicketId(Integer orderId);

	// 某個場次已售出的票（畫座位圖用）
	List<Ticket> findByShowtimeIdOrderByRowNoAscSeatNoAsc(Integer showtimeId);

	// 購票時：這個場次的這個座位是否已售出
	boolean existsByShowtimeIdAndRowNoAndSeatNo(Integer showtimeId, Integer rowNo, Integer seatNo);
}
