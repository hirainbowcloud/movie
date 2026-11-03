package com.example.movie.dao;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.movie.Repository.MemberRepository;
import com.example.movie.Repository.OrdersRepository;
import com.example.movie.Repository.ShowtimeRepository;
import com.example.movie.Repository.TicketRepository;
import com.example.movie.Repository.TicketTypeRepository;
import com.example.movie.exception.MemberNotFoundException;
import com.example.movie.exception.OrdersNotFoundException;
import com.example.movie.exception.ShowtimeNotFoundException;
import com.example.movie.model.Hall;
import com.example.movie.model.Member;
import com.example.movie.model.Orders;
import com.example.movie.model.ShowTime;
import com.example.movie.model.Ticket;
import com.example.movie.model.TicketType;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

// 購票流程：一筆訂單 + N 張票，在同一個交易裡一起存
@Service
public class OrdersService {

	@Autowired
	OrdersRepository ordersRepository;

	@Autowired
	TicketRepository ticketRepository;

	@Autowired
	TicketTypeRepository ticketTypeRepository;

	@Autowired
	ShowtimeRepository showtimeRepository;

	@Autowired
	MemberRepository memberRepository;
	
	@PersistenceContext //JPA的EntityManager
	private EntityManager em;   // 新增：用來重新讀取資料庫的資料
	//EntityManager 型別，JPA提供的類別 em是慣用寫法

	private Pageable buildPageable(int page, int size)
	{
		if (page < 1) page = 1;
		if (size < 1 || size > 100) size = 10;

		//Spring 的PageRequest 的第1頁，是從0開始計算；新的訂單排前面
		return PageRequest.of(page - 1, size, Sort.by("orderId").descending());
	}

	// 管理者：查詢全部訂單
	@Transactional(readOnly = true)
	public Page<Orders> findAll(int page, int size)
	{
		return ordersRepository.findAll(buildPageable(page, size));
	}

	// 購票人：查詢自己的訂單
	@Transactional(readOnly = true)
	public Page<Orders> findByMemberId(Integer memberId, int page, int size)
	{
		return ordersRepository.findByMemberId(memberId, buildPageable(page, size));
	}

	@Transactional(readOnly = true)
	public Orders findById(Integer id)
	{
		return ordersRepository.findById(id)
				.orElseThrow(() -> new OrdersNotFoundException(id));
	}

	// ===================== 購票 =====================
	// form  只用到 memberId、showtimeId
	// items 每張票只用到 rowNo、seatNo、typeId
	// 流程：1.全部檢查 → 2.計算每張票價格 → 3.計算訂單金額 → 4.訂單 + 票一起存
	@Transactional
	public Orders create(Orders form, List<Ticket> items)
	{
		// 檢查會員、場次要存在
		Member member = memberRepository.findById(form.getMemberId())
				.orElseThrow(() -> new MemberNotFoundException(form.getMemberId()));
		ShowTime showtime = showtimeRepository.findById(form.getShowtimeId())
				.orElseThrow(() -> new ShowtimeNotFoundException(form.getShowtimeId()));
		Hall hall = showtime.getHall();

		// 至少要買一張票
		if (items == null || items.isEmpty())
		{
			throw new IllegalArgumentException("至少要選一個座位");
		}

		Orders order = new Orders();
		order.setMember(member);         // 寫入 member_id
		order.setShowtime(showtime);     // 寫入 showtime_id
		//order.setOrderTime(LocalDateTime.now()); // 下單時間給資料庫自動產生

		int originalAmount = 0;
		int totalAmount = 0;
		Set<String> picked = new HashSet<>(); // 這次選的座位，例如 "6-12"

		for (Ticket item : items)
		{
			// 座位檢查
			checkSeat(hall, item);

			String seatKey = item.getRowNo() + "-" + item.getSeatNo();
			if (!picked.add(seatKey)) // add 回傳 false = 已經加過 = 重複選同一個座位
			{
				throw new IllegalArgumentException("座位重複：第" + item.getRowNo() + "排第" + item.getSeatNo() + "號");
			}

			/*
			 * if (ticketRepository.existsByShowtimeIdAndRowNoAndSeatNo(
			 * showtime.getShowtimeId(), item.getRowNo(), item.getSeatNo())) { throw new
			 * IllegalArgumentException("座位已售出：第" + item.getRowNo() + "排第" +
			 * item.getSeatNo() + "號"); }
			 */
			
			if (ticketRepository.countSoldSeat(showtime.getShowtimeId(), item.getRowNo(), item.getSeatNo()) > 0)
			{
				throw new IllegalArgumentException("座位已售出：第" + item.getRowNo() + "排第" + item.getSeatNo() + "號");
			}

			// 確認TicketType
			TicketType type = ticketTypeRepository.findById(item.getTypeId())
					.orElseThrow(() -> new IllegalArgumentException("票種不存在：" + item.getTypeId()));

			Ticket ticket = new Ticket();
			ticket.setShowtime(showtime);       // 寫入 showtime_id
			ticket.setTicketType(type);         // 寫入 type_id
			ticket.setRowNo(item.getRowNo());
			ticket.setSeatNo(item.getSeatNo());
			ticket.setPrice(type.getBasePrice());               // 實際售價
			ticket.setOriginalPrice(calcOriginalPrice(type));   // 原價
			order.addTicket(ticket);            // 寫入 order_id

			originalAmount += ticket.getOriginalPrice(); //累計原價
			totalAmount += ticket.getPrice(); //累計實際售價
		}

		// 整張訂單金額總計
		order.setOriginalAmount(originalAmount); // 整張訂單的原價
		order.setTotalAmount(totalAmount); // 整張訂單的實際售價
		order.setDiscountAmount(originalAmount - totalAmount); // 整張訂單的折扣金額

		// 存訂單（cascade = ALL，票會一起存）
		//return ordersRepository.save(order);
		
		Orders saved = ordersRepository.saveAndFlush(order); // 立刻送出 INSERT
		em.refresh(saved);                                   // 從資料庫讀回 order_time
		return saved;
		
		/*
		 * save() 不保證會馬上寫進資料庫，JPA 可能等交易結束才一起送出。
		 * saveAndFlush() 會立刻執行 INSERT，這時資料庫已經依 DEFAULT CURRENT_TIMESTAMP 
		 * 產生了 order_time。*/
	}

	// 座位要在影廳範圍內：排數 1 ~ row_count，座號至少 1
	private void checkSeat(Hall hall, Ticket item)
	{
		if (item.getRowNo() == null || item.getRowNo() < 1 || item.getRowNo() > hall.getRowCount())
		{
			throw new IllegalArgumentException("排數要在 1 ~ " + hall.getRowCount() + " 之間");
		}
		if (item.getSeatNo() == null || item.getSeatNo() < 1)
		{
			throw new IllegalArgumentException("座號至少是 1");
		}
	}

	// 原價 = 售價 ÷ 折扣率，四捨五入到整數；例如 150 ÷ 0.50 = 300
	private int calcOriginalPrice(TicketType type)
	{
		BigDecimal rate = type.getDiscountRate();
		if (rate == null || rate.compareTo(BigDecimal.ZERO) <= 0)
		{
			return type.getBasePrice(); // 折扣率不合理時，原價就等於售價
		}
		return BigDecimal.valueOf(type.getBasePrice())
				.divide(rate, 0, RoundingMode.HALF_UP)
				.intValue();
	}
}
