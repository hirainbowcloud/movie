package com.example.movie.dto;

import java.util.List;

import org.springframework.data.domain.Page;

import lombok.Data;

@Data
public class PageResponse<T> {

	private List<T> content; //從 Spring 的 Page 物件取出這頁的資料清單
	private int page; //目前頁碼
	private int size; //每頁筆數
	private long totalElements; //總筆數，作業上，資料庫有可以查到上萬筆
	private int totalPages; //總頁數
	
	public static <T> PageResponse<T> from(Page<T> p)
    {
		PageResponse<T> res = new PageResponse<>();
        res.setContent(p.getContent()); //放進自己的 PageResponse 物件
        res.setPage(p.getNumber() + 1); // Spring 初始值是0
        res.setSize(p.getSize());
        res.setTotalElements(p.getTotalElements()); //總筆數
        res.setTotalPages(p.getTotalPages()); //總頁數
        return res;
    }	
}
