package com.example.movie.Repository;

import org.springframework.stereotype.Repository;

import com.example.movie.model.Member;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface MemberRepository extends JpaRepository<Member, Integer> {

	//keyword查詢
	 @Query(value = "SELECT m FROM Member m "
             + "WHERE m.username LIKE CONCAT('%', :keyword, '%') "
             + "OR m.name LIKE CONCAT('%', :keyword, '%') "
             + "OR m.phone LIKE CONCAT('%', :keyword, '%') "
             + "OR m.email LIKE CONCAT('%', :keyword, '%')")
	Page<Member> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
	
	// 會員登入會
	Optional<Member> findByUsername(String username);
	
	// 會員註冊：檢查帳號是否已存在
	boolean existsByUsername(String username);
}
