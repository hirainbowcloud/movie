package com.example.movie.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.movie.model.Orders;

@Repository
public interface OrdersRepository extends JpaRepository<Orders,Integer> {

	// 某位會員的訂單（分頁）
	Page<Orders> findByMemberId(Integer memberId, Pageable pageable);
}
