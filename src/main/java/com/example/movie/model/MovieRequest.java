package com.example.movie.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import lombok.Data;

@Data
public class MovieRequest {
	@JsonUnwrapped
	private Movie movie;
	
	private List<MovieImage> images;
}
