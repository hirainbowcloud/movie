package com.example.movie.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// @RestControllerAdvice = @ControllerAdvice + @ResponseBody
// 攔截所有 Controller 拋出的例外，統一轉換成 JSON 錯誤回應
@RestControllerAdvice
public class GlobalExceptionHandler {
	
	// AI提供: 捕獲：資料庫約束違反（如：資料庫欄位 Unique 重複、資料太長等）
	@ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
	public ResponseEntity<Map<String, Object>> handleDataIntegrity(
	        org.springframework.dao.DataIntegrityViolationException e) {
	// 這裡通常是資料庫層級的重複或格式錯誤，歸類為 400 Bad Request
	    return buildError(HttpStatus.BAD_REQUEST, "資料操作違反約束（例如名稱已重複或資料格式不符）");
	}

	// AI提供:  捕獲：事務提交失敗（有時 Jpa 驗證錯誤會包在這層）
	@ExceptionHandler(org.springframework.transaction.TransactionSystemException.class)
	public ResponseEntity<Map<String, Object>> handleTransactionSystem(
	        org.springframework.transaction.TransactionSystemException e) {
	    return buildError(HttpStatus.BAD_REQUEST, "事務提交失敗，請檢查資料正確性");
	}

    // 捕獲：商品不存在（404）
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ProductNotFoundException e) {
        return buildError(HttpStatus.NOT_FOUND, e.getMessage());
    }
    
    // 捕獲：管理員不存在（404）  ← 新增這段
    @ExceptionHandler(AdminNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleAdminNotFound(AdminNotFoundException e) {
        return buildError(HttpStatus.NOT_FOUND, e.getMessage());
    }

    // 捕獲：業務規則違反（400，如：商品名稱重複）
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(IllegalArgumentException e) {
        return buildError(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    // 捕獲：@Valid 驗證失敗（400，可能有多個欄位錯誤）
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException e) {
        // 收集所有驗證失敗的錯誤訊息（一個請求可能有多個欄位違反驗證）
        List<String> errors = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .toList();

        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("errors", errors);                   // 回傳錯誤清單（複數）
        body.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // 捕獲：所有未預期的例外（500，最後防線，放最後）
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception e) {
        // ⚠️ 安全要點：不直接回傳 e.getMessage()，避免洩露系統內部資訊
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "伺服器發生錯誤，請稍後再試");
    }

    // 建立統一錯誤回應格式的輔助方法（Private Helper）
    private ResponseEntity<Map<String, Object>> buildError(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", status.value());
        body.put("error", message);
        body.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(status).body(body);
    }
    
    
}