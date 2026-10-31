package com.example.movie.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// @RestControllerAdvice = @ControllerAdvice + @ResponseBody
// 攔截所有 Controller 拋出的例外，統一轉換成 JSON 錯誤回應
@RestControllerAdvice
public class GlobalExceptionHandler {

	//自訂
	@ExceptionHandler(AdminNotFoundException.class)
	public ResponseEntity<String> handleAdminNotFound(AdminNotFoundException e) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404
	}

	@ExceptionHandler(MemberNotFoundException.class)
	public ResponseEntity<String> handleMemberNotFound(MemberNotFoundException e) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404
	}

	@ExceptionHandler(MovieNotFoundException.class)
	public ResponseEntity<String> handleMovieNotFound(MovieNotFoundException e) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage()); // 404
	}

	@ExceptionHandler(UsernameAlreadyExistsException.class)
	public ResponseEntity<String> handleUsernameExists(UsernameAlreadyExistsException e) {
		return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage()); // 409
	}

	// AI提供: 捕獲：資料庫約束違反（如：資料庫欄位 Unique 重複、資料太長等）
	@ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
	public ResponseEntity<Map<String, Object>> handleDataIntegrity(
			org.springframework.dao.DataIntegrityViolationException e) {
		// 這裡通常是資料庫層級的重複或格式錯誤，歸類為 400 Bad Request
		return buildError(HttpStatus.BAD_REQUEST, "資料操作違反約束（例如名稱已重複或資料格式不符）");
	}

	// AI提供: 捕獲：事務提交失敗（有時 Jpa 驗證錯誤會包在這層）
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

	// 捕獲：業務規則違反（400，如：商品名稱重複）
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<Map<String, Object>> handleBadRequest(IllegalArgumentException e) {
		return buildError(HttpStatus.BAD_REQUEST, e.getMessage());
	}

	// 捕獲：@Valid 驗證失敗（400，可能有多個欄位錯誤）
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException e) {
		// 收集所有驗證失敗的錯誤訊息（一個請求可能有多個欄位違反驗證）
		List<String> errors = e.getBindingResult().getFieldErrors().stream().map(FieldError::getDefaultMessage)
				.toList();

		Map<String, Object> body = new HashMap<>();
		body.put("status", HttpStatus.BAD_REQUEST.value());
		body.put("errors", errors); // 回傳錯誤清單（複數）
		body.put("timestamp", LocalDateTime.now());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
	}

	// 捕獲：網址打錯、找不到對應的 API（404）
	@ExceptionHandler(NoResourceFoundException.class)
	public ResponseEntity<Map<String, Object>> handleNoResource(NoResourceFoundException e) {
		return buildError(HttpStatus.NOT_FOUND, "找不到此 API 路徑：" + e.getResourcePath());
	}

	// 捕獲：multipart 缺少必要的 part（例如沒傳 movie）→ 400
	@ExceptionHandler(org.springframework.web.multipart.support.MissingServletRequestPartException.class)
	public ResponseEntity<Map<String, Object>> handleMissingPart(
			org.springframework.web.multipart.support.MissingServletRequestPartException e) {
		return buildError(HttpStatus.BAD_REQUEST, "缺少必要欄位：" + e.getRequestPartName());
	}

	// 捕獲：JSON 格式錯誤（少逗號、日期格式不是 yyyy-MM-dd…）→ 400
	@ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
	public ResponseEntity<Map<String, Object>> handleNotReadable(
			org.springframework.http.converter.HttpMessageNotReadableException e) {
		return buildError(HttpStatus.BAD_REQUEST, "JSON 格式錯誤，請檢查欄位與日期格式（yyyy-MM-dd）");
	}

	// 捕獲：Content-Type 不支援（Postman 的 movie / images 沒設 application/json）→ 415
	@ExceptionHandler(org.springframework.web.HttpMediaTypeNotSupportedException.class)
	public ResponseEntity<Map<String, Object>> handleMediaType(
			org.springframework.web.HttpMediaTypeNotSupportedException e) {
		return buildError(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Content-Type 不支援：" + e.getContentType());
	}

	// 捕獲：網址參數型別錯誤（例如 /api/movie/abc，id 應該是數字）→ 400
	@ExceptionHandler(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class)
	public ResponseEntity<Map<String, Object>> handleTypeMismatch(
			org.springframework.web.method.annotation.MethodArgumentTypeMismatchException e) {
		return buildError(HttpStatus.BAD_REQUEST, "參數格式錯誤：" + e.getName());
	}

	// 捕獲：上傳檔案超過大小上限（application.properties 的 max-file-size）→ 413
	@ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
	public ResponseEntity<Map<String, Object>> handleMaxUpload(
			org.springframework.web.multipart.MaxUploadSizeExceededException e) {
		return buildError(HttpStatus.PAYLOAD_TOO_LARGE, "檔案太大，超過上傳上限");
	}

	// 捕獲：所有未預期的例外（500，最後防線，放最後）
	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, Object>> handleGeneral(Exception e) {
		// ⚠️ 安全要點：不直接回傳 e.getMessage()，避免洩露系統內部資訊
		e.printStackTrace(); // 但要在 Console 印出來，才知道真正的錯誤原因
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