package com.example.movie.dao;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.movie.Repository.TicketTypeRepository;
import com.example.movie.exception.TicketTypeNotFoundException;
import com.example.movie.model.TicketType;

@Service
public class TicketTypeService {

	@Autowired
	TicketTypeRepository ticketTypeRepository;

	private Pageable buildPageable(int page, int size)
	{
		if (page < 1) page = 1;
		if (size < 1 || size > 100) size = 10;

		//Spring 的PageRequest 的第1頁，是從0開始計算
		return PageRequest.of(page - 1, size, Sort.by("typeId").ascending());
	}

	public Page<TicketType> findAll(int page, int size)
	{
		return ticketTypeRepository.findAll(buildPageable(page, size));
	}

	@Transactional(readOnly = true)
	public TicketType findById(Integer id)
	{
		return ticketTypeRepository.findById(id)
				.orElseThrow(() -> new TicketTypeNotFoundException(id));
	}


	@Transactional
	public TicketType create(TicketType ticketType)
	{
		Optional<TicketType> t = ticketTypeRepository.findByTypeName(ticketType.getTypeName());
		if (t.isPresent())
		{
			throw new IllegalArgumentException("票種名稱已存在：" + ticketType.getTypeName());
		}
		return ticketTypeRepository.save(ticketType);
	}

	@Transactional
	public TicketType update(Integer typeId, TicketType ticketType)
	{
		TicketType existing = ticketTypeRepository.findById(typeId)
				.orElseThrow(() -> new TicketTypeNotFoundException(typeId));

		Optional<TicketType> sameName = ticketTypeRepository.findByTypeName(ticketType.getTypeName());
		if (sameName.isPresent() && !sameName.get().getTypeId().equals(typeId))
		{
			throw new IllegalArgumentException("票種名稱已被使用：" + ticketType.getTypeName());
		}

		// typeId 不改；已賣出的票價格不受影響（ticket 存的是當時的價格）
		existing.setTypeName(ticketType.getTypeName());
		existing.setBasePrice(ticketType.getBasePrice());
		existing.setDiscountRate(ticketType.getDiscountRate());
		existing.setDescription(ticketType.getDescription());
		return ticketTypeRepository.save(existing);
	}

	// 已經有票使用的票種，資料庫外鍵會擋下刪除 → 400
	@Transactional
	public Optional<TicketType> delete(Integer typeId)
	{
		Optional<TicketType> t = ticketTypeRepository.findById(typeId);
		if (t.isPresent())
		{
			ticketTypeRepository.delete(t.get());
			ticketTypeRepository.flush(); // 現在就執行 DELETE，外鍵錯誤在這裡就會丟出來
		}
		return t;
	}
}
