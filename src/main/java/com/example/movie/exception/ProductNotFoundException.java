package com.example.movie.exception;

//繼承 RuntimeException：不需要在方法簽名宣告 throws（Unchecked Exception）
public class ProductNotFoundException extends RuntimeException {

public ProductNotFoundException(Long id) {
   super("不存在，id: " + id);
}

public ProductNotFoundException(String message) {
   super(message);
}
}

//Exception父類別Throwable
//unchkecException: RuntimeException
//chkecException: IOException、SQLException