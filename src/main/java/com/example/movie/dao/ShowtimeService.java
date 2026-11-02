package com.example.movie.dao;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.movie.Repository.HallRepository;
import com.example.movie.Repository.MovieRepository;
import com.example.movie.Repository.ShowtimeRepository;
import com.example.movie.exception.HallNotFoundException;
import com.example.movie.exception.MovieNotFoundException;
import com.example.movie.exception.ShowtimeNotFoundException;
import com.example.movie.model.Hall;
import com.example.movie.model.Movie;
import com.example.movie.model.ShowTime;

@Service
public class ShowtimeService {

	@Autowired
	ShowtimeRepository showtimeRepository;

	@Autowired
	MovieRepository movieRepository;

	@Autowired
	HallRepository hallRepository;
	
	private Pageable buildPageable(int page, int size)
	{
		if (page < 1) page = 1;
		if (size < 1 || size > 100) size = 10;
		
		//Spring 的PageRequest 的第1頁，是從0開始計算
		return PageRequest.of(page - 1, size, Sort.by("showtimeId").ascending());
	}
	
	public Page<ShowTime> findAll(int page, int size)
	{
		return showtimeRepository.findAll(buildPageable(page,size));
	}

	// 某部電影的場次（購票人選場次用）
	public Page<ShowTime> findByMovieId(Integer movieId, int page, int size)
	{
		return showtimeRepository.findByMovieId(movieId, buildPageable(page, size));
	}

	@Transactional(readOnly = true)
	public ShowTime findById(Integer id)
	{
		return showtimeRepository.findById(id)
				.orElseThrow(() -> new ShowtimeNotFoundException(id));
	}

	// form 只用到 movieId、hallId、showDate、startTime
	@Transactional
	public ShowTime create(ShowTime form)
	{
		Movie movie = movieRepository.findById(form.getMovieId())
				.orElseThrow(() -> new MovieNotFoundException(form.getMovieId()));
		Hall hall = hallRepository.findById(form.getHallId())
				.orElseThrow(() -> new HallNotFoundException(form.getHallId()));

		checkShowDate(movie, form);

		if (showtimeRepository.existsByHallIdAndShowDateAndStartTime(
				form.getHallId(), form.getShowDate(), form.getStartTime()))
		{
			throw new IllegalArgumentException("此影廳在這個時間已有場次");
		}

		ShowTime showtime = new ShowTime();
		showtime.setMovie(movie);      // 寫入 movie_id
		showtime.setHall(hall);        // 寫入 hall_id
		showtime.setShowDate(form.getShowDate());
		showtime.setStartTime(form.getStartTime());
		return showtimeRepository.save(showtime);
	}

	@Transactional
	public ShowTime update(Integer showtimeId, ShowTime form)
	{
		ShowTime existing = showtimeRepository.findById(showtimeId)
				.orElseThrow(() -> new ShowtimeNotFoundException(showtimeId));

		Movie movie = movieRepository.findById(form.getMovieId())
				.orElseThrow(() -> new MovieNotFoundException(form.getMovieId()));
		Hall hall = hallRepository.findById(form.getHallId())
				.orElseThrow(() -> new HallNotFoundException(form.getHallId()));

		checkShowDate(movie, form);

		if (showtimeRepository.existsByHallIdAndShowDateAndStartTimeAndShowtimeIdNot(
				form.getHallId(), form.getShowDate(), form.getStartTime(), showtimeId))
		{
			throw new IllegalArgumentException("此影廳在這個時間已有場次");
		}

		existing.setMovie(movie);
		existing.setHall(hall);
		existing.setShowDate(form.getShowDate());
		existing.setStartTime(form.getStartTime());
		return showtimeRepository.save(existing);
	}

	// 已經有訂單、票的場次，資料庫外鍵會擋下刪除 → 400
	@Transactional
	public Optional<ShowTime> delete(Integer showtimeId)
	{
		Optional<ShowTime> s = showtimeRepository.findById(showtimeId);
		if (s.isPresent())
		{
			showtimeRepository.delete(s.get());
			showtimeRepository.flush(); // 現在就執行 DELETE，外鍵錯誤在這裡就會丟出來
		}
		return s;
	}

	// 放映日期不能早於電影上映日期
	private void checkShowDate(Movie movie, ShowTime form)
	{
		if (form.getShowDate().isBefore(movie.getReleaseDate()))
		{
			throw new IllegalArgumentException("放映日期不能早於上映日期：" + movie.getReleaseDate());
		}
	}
}
