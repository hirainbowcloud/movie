package com.example.movie.Repository;

import org.springframework.stereotype.Repository;

import com.example.movie.model.Member;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Integer> {

	//keyword查詢
	 @Query(value = "SELECT * FROM member "
             + "WHERE username LIKE CONCAT('%', :keyword, '%') "
             + "OR name LIKE CONCAT('%', :keyword, '%') "
             + "OR phone LIKE CONCAT('%', :keyword, '%') "
             + "OR email LIKE CONCAT('%', :keyword, '%')",
       nativeQuery = true)
	List<Member> searchByKeyword(@Param("keyword") String keyword);
	
	// 會員登入會
	Optional<Member> findByUsername(String username);
	
	// 會員註冊：檢查帳號是否已存在
	boolean existsByUsername(String username);
}
