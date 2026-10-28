package com.example.movie.exception;

//繼承 RuntimeException：不需要在方法簽名宣告 throws（Unchecked Exception）
public class AdminNotFoundException extends RuntimeException {

public AdminNotFoundException(Integer id) {
   super("不存在，admin_Id: " + id);
}

public AdminNotFoundException(String message) {
   super(message);
}
}
