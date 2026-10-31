package com.example.movie.Repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.movie.model.Hall;

@Repository
public interface HallRepository extends JpaRepository<Hall,Integer> {

	Optional<Hall> findByHallName(String hallName);

	@Query(value = "SELECT h FROM Hall h WHERE h.hallName LIKE CONCAT('%', :keyword, '%')")
	Page<Hall> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
