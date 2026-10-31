package com.example.movie.dto;

import java.util.List;

import org.springframework.data.domain.Page;

import lombok.Data;

@Data
public class PageResponse<T> {

	private List<T> content; //這頁資料
	private int page; //目前頁碼
	private int size; //每頁筆數
	private long totalElements; //總筆數
	private int totalPages; //總頁數
	
	public static <T> PageResponse<T> from(Page<T> p)
    {
		PageResponse<T> res = new PageResponse<>();
        res.setContent(p.getContent());
        res.setPage(p.getNumber() + 1);   // Spring 從 0 開始，轉成從 1 開始
        res.setSize(p.getSize());
        res.setTotalElements(p.getTotalElements());
        res.setTotalPages(p.getTotalPages());
        return res;
    }	
}
