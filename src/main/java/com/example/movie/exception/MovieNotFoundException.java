package com.example.movie.exception;

public class MovieNotFoundException extends RuntimeException {

	public MovieNotFoundException(Integer id) {
	   super("不存在，movie_id: " + id);
	}

	public MovieNotFoundException(String message) {
	   super(message);
	}

}
