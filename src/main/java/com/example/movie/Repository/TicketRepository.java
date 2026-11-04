package com.example.movie.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.movie.model.Ticket;

@Repository
public interface TicketRepository extends JpaRepository<Ticket,Integer> {

	// 某筆訂單的票
	List<Ticket> findByOrderIdOrderByTicketId(Integer orderId);

	// showtime_id 是這個場次的資料。但它搜的是 ticket 表，所以結果就是「這個場次已售出的座位」
	//List<Ticket> findByShowtimeIdOrderByRowNoAscSeatNoAsc(Integer showtimeId);
	@Query(value = "SELECT * FROM ticket WHERE showtime_id = :showtimeId ORDER BY row_no ASC, seat_no ASC",
		       nativeQuery = true)
	List<Ticket> findSoldSeats(@Param("showtimeId") Integer showtimeId);
	

	// 購票時檢查某一個座位能不能賣
	//場次 1 的第 5 排第 4 號，有沒有人買過？
	//SELECT t.ticket_id FROM ticket t 
	//WHERE t.showtime_id = 1 AND t.row_no = 5 AND t.seat_no = 4
	//		LIMIT 1;
	//boolean existsByShowtimeIdAndRowNoAndSeatNo(Integer showtimeId, Integer rowNo, Integer seatNo);
	@Query(value = "SELECT COUNT(*) FROM ticket " +
            "WHERE showtime_id = :showtimeId AND row_no = :rowNo AND seat_no = :seatNo",
            nativeQuery = true)
			int countSoldSeat(
					@Param("showtimeId") Integer showtimeId,
					@Param("rowNo") Integer rowNo,
					@Param("seatNo") Integer seatNo);
	default boolean existsSoldoutSeat(Integer showtimeId, Integer rowNo, Integer seatNo)
	{
		return countSoldSeat(showtimeId, rowNo, seatNo) > 0;
	}
	
}
