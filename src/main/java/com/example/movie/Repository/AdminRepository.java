package com.example.movie.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.movie.model.Admin;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Integer> {

	Optional<Admin> findByUsername(String username);
	
	Optional<Admin> findByName(String name);
	
	@Query(value = "SELECT * FROM admin WHERE username LIKE CONCAT('%', :keyword, '%') OR name LIKE CONCAT('%', :keyword, '%')",
		       nativeQuery = true)
	List<Admin> searchByKeyword(@Param("keyword") String keyword);
}
