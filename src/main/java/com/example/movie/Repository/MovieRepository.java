package com.example.movie.Repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import com.example.movie.model.Movie;

public interface MovieRepository extends JpaRepository<Movie,Integer>{

	// 查詢全部片名，排除下架
	@Query(value = "SELECT * FROM movie WHERE is_active = 1",nativeQuery = true)
	
	List<Movie> findIsActive();
	
	// 查詢上架中的電影（分頁）：Spring Data 依方法名稱自動產生 WHERE is_active = true
	Page<Movie> findByIsActiveTrue(Pageable pageable);
	
	// 查詢下架的電影（分頁）：WHERE is_active = false
	Page<Movie> findByIsActiveFalse(Pageable pageable);
	
	// 查詢「片名 + 上映日期」是否已存在	
	@Query(value = "SELECT COUNT(*) FROM movie WHERE title = :title AND release_date = :releaseDate",nativeQuery = true)
	
	int countMysqlTitleAndReleaseDate(@Param("title") String title, @Param("releaseDate") LocalDate releaseDate);
	
	// 查詢「片名 + 上映日期」是否已被「別部電影」使用（修改時用）
	@Query(value = "SELECT COUNT(*) FROM movie WHERE title = :title AND release_date = :releaseDate AND movie_id <> :movieId",
			nativeQuery = true)
	int countMysqlTitleAndReleaseDateExcludeId(@Param("title") String title,
			@Param("releaseDate") LocalDate releaseDate,
			@Param("movieId") Integer movieId);
	
}
