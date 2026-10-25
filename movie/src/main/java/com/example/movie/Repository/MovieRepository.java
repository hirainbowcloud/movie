package com.example.movie.Repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import com.example.movie.model.Movie;

public interface MovieRepository extends JpaRepository<Movie,Integer>{

	// 查詢全部片名，排除下架
	@Query(value = "SELECT * FROM movie WHERE is_active = 1",nativeQuery = true)
	
	List<Movie> findIsActive();
	
	// 查詢「片名 + 上映日期」是否已存在	
	@Query(value = "SELECT COUNT(*) FROM movie WHERE title = :title AND release_date = :releaseDate",nativeQuery = true)
	
	int countMysqlTitleAndReleaseDate(@Param("title") String title, @Param("releaseDate") LocalDate releaseDate);
		
	
}
