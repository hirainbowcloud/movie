package com.example.movie.exception;

//繼承 RuntimeException：不需要在方法簽名宣告 throws（Unchecked Exception）
public class HallNotFoundException extends RuntimeException {

	public HallNotFoundException(Integer id) {
	   super("不存在，hall_id: " + id);
	}

	public HallNotFoundException(String message) {
	   super(message);
	}

}
