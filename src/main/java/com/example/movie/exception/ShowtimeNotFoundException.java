package com.example.movie.exception;

//繼承 RuntimeException：不需要在方法簽名宣告 throws（Unchecked Exception）
public class ShowtimeNotFoundException extends RuntimeException {

	public ShowtimeNotFoundException(Integer id) {
	   super("不存在，showtime_id: " + id);
	}

	public ShowtimeNotFoundException(String message) {
	   super(message);
	}

}
