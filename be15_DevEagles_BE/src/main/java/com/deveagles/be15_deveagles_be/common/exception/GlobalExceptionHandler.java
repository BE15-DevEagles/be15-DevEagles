package com.deveagles.be15_deveagles_be.common.exception;

import com.deveagles.be15_deveagles_be.common.dto.ApiResponse;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.exception.ChatBusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
    ErrorCode errorCode = e.getErrorCode();
    ApiResponse<Void> response = ApiResponse.failure(errorCode.getCode(), errorCode.getMessage());
    return new ResponseEntity<>(response, errorCode.getHttpStatus());
  }

  @ExceptionHandler(ChatBusinessException.class)
  public ResponseEntity<ApiResponse<Void>> handleChatBusinessException(ChatBusinessException e) {
    log.error("ChatBusinessException: {}", e.getMessage(), e);
    ApiResponse<Void> response =
        ApiResponse.failure(e.getErrorCode().getCode(), e.getErrorCode().getMessage());
    return new ResponseEntity<>(response, e.getErrorCode().getHttpStatus());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Void>> handleValidationException(
      MethodArgumentNotValidException e) {

    ErrorCode errorCode = ErrorCode.VALIDATION_ERROR;
    StringBuilder errorMessage = new StringBuilder(errorCode.getMessage());
    for (FieldError error : e.getBindingResult().getFieldErrors()) {
      errorMessage.append(String.format("[%s : %s]", error.getField(), error.getDefaultMessage()));
    }

    ApiResponse<Void> response = ApiResponse.failure(errorCode.getCode(), errorMessage.toString());
    return new ResponseEntity<>(response, errorCode.getHttpStatus());
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
    log.error("Unhandled Exception: {}", e.getMessage(), e);
    ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
    ApiResponse<Void> response = ApiResponse.failure(errorCode.getCode(), errorCode.getMessage());
    return new ResponseEntity<>(response, errorCode.getHttpStatus());
  }
}
