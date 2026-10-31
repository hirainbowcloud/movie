package com.example.movie.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.movie.model.Admin;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Integer> {

	Optional<Admin> findByUsername(String username);
	
	Optional<Admin> findByName(String name);
	
	@Query(value = "SELECT a FROM Admin a WHERE a.username LIKE CONCAT('%', :keyword, '%') OR a.name LIKE CONCAT('%', :keyword, '%')")
	Page<Admin> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
