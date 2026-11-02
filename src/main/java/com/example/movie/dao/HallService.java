package com.example.movie.dao;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.movie.exception.HallNotFoundException;
import com.example.movie.exception.MovieNotFoundException;
import com.example.movie.Repository.HallRepository;
import com.example.movie.Repository.MovieRepository;
import com.example.movie.Repository.ShowtimeRepository;
import com.example.movie.model.Hall;
import com.example.movie.model.Movie;
import com.example.movie.model.ShowTime;

@Service
public class HallService {

	@Autowired
	HallRepository hallRepository;
	
	@Autowired
	MovieRepository movieRepository;
	
	@Autowired
	ShowtimeRepository showtimeRepository;
	
	private Pageable buildPageable(int page, int size)
	{
		if (page < 1) page = 1;
		if (size < 1 || size > 100) size = 10;
		
		//Spring 的PageRequest 的第1頁，是從0開始計算
		return PageRequest.of(page - 1, size, Sort.by("hallId").ascending());
	}
	
	public Page<Hall> findAll(int page, int size)
	{
		return hallRepository.findAll(buildPageable(page, size));
	}
	
	
	public Optional<Hall> findByHallName(String hallName)
	{
		return hallRepository.findByHallName(hallName);
	}
	
	@Transactional(readOnly = true)
	public Hall findById(Integer id)
	{
		return hallRepository.findById(id)
				.orElseThrow(() -> new HallNotFoundException(id));
	}
	
	public Page<Hall> searchByKeyword(String keyword, int page, int size)
	{
		if(keyword == null || keyword.isBlank())
		{
			return hallRepository.findAll(buildPageable(page, size)); //page 目前頁碼，size 目前筆數
		}
		return hallRepository.searchByKeyword(keyword, buildPageable(page, size));
	}
	
	@Transactional
	public Hall create(Hall hall)
	{
		Optional<Hall> h = hallRepository.findByHallName(hall.getHallName());
		if(h.isPresent())
		{
			throw new IllegalArgumentException("廳名已存在：" + hall.getHallName());
		}
		return hallRepository.save(hall);
		
	}
	
	//在 @Transactional 方法裡，呼叫 delete() 時 
	//JPA 不會馬上對資料庫執行 DELETE。它會先記下來，等方法結束、交易 commit 時才一起執行。
	@Transactional
	public Optional<Hall> delete(Integer hallId)
	{		
		Optional<Hall> h = hallRepository.findById(hallId);
		if(h.isPresent())
		{
			hallRepository.delete(h.get());
			hallRepository.flush(); // 現在就執行 DELETE，外鍵錯誤在這裡就會丟出來
		}
		
		return h;
	}
	
	@Transactional
	public Hall update(Integer hallId, Hall hall)
	{
		Hall existing = hallRepository.findById(hallId)
				.orElseThrow(() -> new HallNotFoundException(hallId));
		
		Optional<Hall> sameName = hallRepository.findByHallName(hall.getHallName());
		   if (sameName.isPresent() && !sameName.get().getHallId().equals(hallId))
		   {
		       throw new IllegalArgumentException("廳名已被使用：" + hall.getHallName());
		   }
		   
		existing.setHallName(hall.getHallName());
		existing.setRowCount(hall.getRowCount());
		existing.setFirstRowSeats(hall.getFirstRowSeats());

	    return hallRepository.save(existing);
		
	}
	
	@Transactional
	public List<ShowTime> createWithShowtimes(Hall hall, List<ShowTime> items)
	{
		Hall saveHall = create(hall);
		Set<String> times = new HashSet<>();
		List<ShowTime> result = new ArrayList<>();
		
		for(ShowTime show: items)
		{
			Movie movie = movieRepository.findById(show.getMovieId())
				  .orElseThrow(() -> new MovieNotFoundException(show.getMovieId()));
			
			if(show.getShowDate().isBefore(movie.getReleaseDate()))
			{
				throw new IllegalArgumentException(
					movie.getTitle() + " 的放映日期不能早於上映日期：" + movie.getReleaseDate());
			}
			
			String key = show.getShowDate()+" "+show.getStartTime();
			if(!times.add(key))
			{
				throw new IllegalArgumentException("場次時間重複：" + key);
			}
			
			ShowTime s = new ShowTime();
			s.setMovie(movie); 
			s.setHall(saveHall);
			s.setShowDate(show.getShowDate());
			s.setStartTime(show.getStartTime());
			result.add(showtimeRepository.save(s));
		}
		
		return result;
	}
}
