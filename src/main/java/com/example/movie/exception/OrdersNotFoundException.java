package com.example.movie.exception;

//繼承 RuntimeException：不需要在方法簽名宣告 throws（Unchecked Exception）
public class OrdersNotFoundException extends RuntimeException {

	public OrdersNotFoundException(Integer id) {
	   super("不存在，order_id: " + id);
	}

	public OrdersNotFoundException(String message) {
	   super(message);
	}

}
