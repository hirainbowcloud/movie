package com.example.movie.exception;

public class MemberNotFoundException extends RuntimeException{
	public MemberNotFoundException(Integer id) {
		   super("不存在，member_id: " + id);
		}

		public MemberNotFoundException(String message) {
		   super(message);
		}
}
