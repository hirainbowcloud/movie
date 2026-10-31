package com.example.movie.dto;

import java.time.LocalDateTime;

import com.example.movie.model.MovieImage;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@JsonPropertyOrder({ "imageId", "movieId", "imageUrl", "imageType", "sortOrder", "createdAt" })
@Data
public class MovieImageResponse {

	private Integer imageId;
	private Integer movieId;
	private String imageUrl;
	private String imageType;
	private Integer sortOrder;
	private LocalDateTime createdAt;

	public static MovieImageResponse from(MovieImage img)
	{
		MovieImageResponse res = new MovieImageResponse();
		res.setImageId(img.getImageId());
		res.setMovieId(img.getMovieId());
		res.setImageUrl(img.getImageUrl());
		res.setImageType(img.getImageType());
		res.setSortOrder(img.getSortOrder());
		res.setCreatedAt(img.getCreatedAt());
		return res;
	}
}
