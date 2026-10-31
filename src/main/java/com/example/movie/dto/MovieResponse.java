package com.example.movie.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.example.movie.model.Movie;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@JsonPropertyOrder
({ "movieId","title", "durationMin", "rating", "synopsis",
	"director", "actors","genre", "releaseDate", "trailerUrl",
	"isActive", "updatedAt","images" })
@Data
public class MovieResponse {

	private Integer movieId;
	private String title;
	private Integer durationMin;
	private String rating;
	private String synopsis;
	private String director;
	private String actors;
	private String genre;
	private LocalDate releaseDate;
	private String trailerUrl;
	private Boolean isActive;
	private LocalDateTime updatedAt;
	private List<MovieImageResponse> images;

	public static MovieResponse from(Movie movie)
	{
		MovieResponse res = new MovieResponse();
		res.setMovieId(movie.getMovieId());
		res.setTitle(movie.getTitle());
		res.setDurationMin(movie.getDurationMin());
		res.setRating(movie.getRating());
		res.setSynopsis(movie.getSynopsis());
		res.setDirector(movie.getDirector());
		res.setActors(movie.getActors());
		res.setGenre(movie.getGenre());
		res.setReleaseDate(movie.getReleaseDate());
		res.setTrailerUrl(movie.getTrailerUrl());
		res.setIsActive(movie.getIsActive());
		res.setUpdatedAt(movie.getUpdatedAt());
		res.setImages(movie.getImages()
				.stream()
				.map(MovieImageResponse::from)
				.toList());
		return res;
	}
}
