package com.example.movie.exception;

//繼承 RuntimeException：不需要在方法簽名宣告 throws（Unchecked Exception）
public class TicketTypeNotFoundException extends RuntimeException {

	public TicketTypeNotFoundException(Integer id) {
	   super("不存在，type_id: " + id);
	}

	public TicketTypeNotFoundException(String message) {
	   super(message);
	}

}
