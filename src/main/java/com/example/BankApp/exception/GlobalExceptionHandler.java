package com.example.BankApp.exception;

import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  /**
   * 不正な引数が渡された場合、HTTPステータス 400 Bad Request を返します。
   */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, String>> handleIllegalArgumentException(
      IllegalArgumentException e) {
    Map<String, String> errorResponse = Map.of("error", e.getMessage());
    return ResponseEntity.badRequest().body(errorResponse);
  }

  /**
   * 存在しない口座番号が指定された場合、HTTPステータス 404 Not Found を返します。
   */
  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<Map<String, String>> handleResourceNotFound(ResourceNotFoundException e) {
    Map<String, String> errorResponse = Map.of("error", e.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
  }

  /**
   * リクエストボディのバリデーションエラーを処理します。
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, String>> handleValidationExceptions(
      MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getFieldErrors().forEach(error ->
        errors.put(error.getField(), error.getDefaultMessage()));
    return ResponseEntity.badRequest().body(errors);
  }

  /**
   * メソッド引数（@PathVariable や @RequestParam）のバリデーションエラーを処理します。
   */
  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<Map<String, String>> handleConstraintViolation(
      ConstraintViolationException ex) {
    Map<String, String> errors = new HashMap<>();
    ex.getConstraintViolations().forEach(violation ->
        errors.put("field", violation.getMessage()));
    return ResponseEntity.badRequest().body(errors);
  }

  /**
   * アクセス権限がない場合、HTTPステータス 403 Forbidden を返します。
   * 例：他人の口座にアクセスしようとした場合など。

   */
  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<Map<String, String>> handleAccessDenied(AccessDeniedException ex) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(Map.of("error", "この口座に対する権限がありません"));
  }

  /**
   * その他の予期しないエラーが発生した場合、HTTPステータス 500 Internal Server Error を返します。 アプリケーションのバグやシステム障害など。
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, String>> handleGeneralException(Exception ex) {
    Map<String, String> error = new HashMap<>();
    error.put("error", "予期せぬエラーが発生しました。");
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
  }
}
