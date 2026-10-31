package com.example.movie.exception;

public class UsernameAlreadyExistsException extends RuntimeException {

	public UsernameAlreadyExistsException(String username) {
        super("帳號已存在，username: " + username);
    }
}
