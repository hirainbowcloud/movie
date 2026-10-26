package com.example.movie.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.movie.model.MovieImage;

@Repository
public interface MovieImageRepository extends JpaRepository<MovieImage, Integer> {

	// 查詢某部電影的所有圖片
	// List<MovieImage> findByMovieIdOrderBySortOrderAsc(Integer movieId);
	@Query(value = "select * from movie_image where movie_id = :movieid order by sort_order asc", nativeQuery = true)
	
	List<MovieImage> findMovieImageByMovieId(@Param("movieid") Integer movieId);

	// 刪除某部電影的全部圖片，回傳刪除筆數
	// 注意：直接下 SQL，會繞過 JPA 的 cascade / orphanRemoval
	// clearAutomatically = true → 執行後清掉 JPA 快取，避免讀到已刪除的舊資料
	@Modifying(clearAutomatically = true)
	@Transactional
	@Query(value = "DELETE FROM movie_image WHERE movie_id = :movieId", nativeQuery = true)
	
	int deleteByMovieIdNative(@Param("movieId") Integer movieId);

}
