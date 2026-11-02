package com.example.movie.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.movie.model.ShowTime;

@Repository
public interface ShowtimeRepository extends JpaRepository<ShowTime,Integer> {

	// 某部電影的場次（分頁）
	Page<ShowTime> findByMovieId(Integer movieId, Pageable pageable);

	// 新增時：同一影廳、同一日期時間是否已有場次
	boolean existsByHallIdAndShowDateAndStartTime(Integer hallId, LocalDate showDate, LocalTime startTime);

	// 修改時：同一影廳、同一日期時間是否已被「別的場次」使用
	boolean existsByHallIdAndShowDateAndStartTimeAndShowtimeIdNot(
			Integer hallId, LocalDate showDate, LocalTime startTime, Integer showtimeId);

	// 購票人：某部電影今天以後的場次，依日期、開演時間排序
	List<ShowTime> findByMovieIdAndShowDateGreaterThanEqualOrderByShowDateAscStartTimeAsc(
			Integer movieId, LocalDate showDate);
}
