package com.example.movie.dto;

import java.util.List;

import com.example.movie.model.Movie;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor      // 一旦加了 @AllArgsConstructor，無參數建構子就不會自動產生，要明確補上
@AllArgsConstructor     // 產生 Info(String message, Movie movie, List<MovieImage> images)
@JsonInclude(JsonInclude.Include.NON_NULL)   // null 的欄位不輸出
public class Info 
{
	private String message;
	@JsonUnwrapped
	private Movie movie;  // movie 裡已經有 images
	
	private List<Movie> movies;
}
