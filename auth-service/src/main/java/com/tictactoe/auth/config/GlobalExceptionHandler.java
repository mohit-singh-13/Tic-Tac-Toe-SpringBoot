package com.tictactoe.auth.config;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(exception = MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {

		Map<String, String> fieldErrors = new HashMap<>();
		for (FieldError error : ex.getBindingResult().getFieldErrors()) {
			fieldErrors.put(error.getField(), error.getDefaultMessage());
		}

		Map<String, Object> body = new HashMap<>();
		body.put("errors", fieldErrors);
		body.put("status", HttpStatus.BAD_REQUEST.value());
		body.put("timestamp", LocalDateTime.now().toString());

		return ResponseEntity.badRequest().body(body);
	}

	@ExceptionHandler(exception = IllegalArgumentException.class)
	public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {

		Map<String, Object> body = new HashMap<>();
		body.put("error", ex.getMessage());
		body.put("status", HttpStatus.BAD_REQUEST.value());
		body.put("timestamp", LocalDateTime.now().toString());

		return ResponseEntity.badRequest().body(body);
	}

	@ExceptionHandler(exception = Exception.class)
	public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {

		Map<String, Object> body = new HashMap<>();
		body.put("error", "An unexpected error occurred");
		body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
		body.put("timestamp", LocalDateTime.now().toString());

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
	}
}
