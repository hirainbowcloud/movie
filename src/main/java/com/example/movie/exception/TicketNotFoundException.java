package com.example.movie.exception;

//繼承 RuntimeException：不需要在方法簽名宣告 throws（Unchecked Exception）
public class TicketNotFoundException extends RuntimeException {

	public TicketNotFoundException(Integer id) {
	   super("不存在，ticket_id: " + id);
	}

	public TicketNotFoundException(String message) {
	   super(message);
	}

}
