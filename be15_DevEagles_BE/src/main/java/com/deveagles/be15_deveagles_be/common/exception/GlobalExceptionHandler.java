package com.deveagles.be15_deveagles_be.common.exception;

import com.deveagles.be15_deveagles_be.common.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  // 비즈니스 예외 처리
  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
    log.error("[비즈니스 예외] {}", e.getMessage());
    ApiResponse<Void> response = ApiResponse.failure(e.getCode(), e.getOriginalMessage());
    return new ResponseEntity<>(response, e.getHttpStatus());
  }

  // 유효성 검증 예외 처리
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Void>> handleValidationException(
      MethodArgumentNotValidException e) {
    ErrorCode errorCode = ErrorCode.VALIDATION_ERROR;
    StringBuilder errorMessage = new StringBuilder(errorCode.getMessage());

    for (FieldError error : e.getBindingResult().getFieldErrors()) {
      errorMessage.append(String.format(" [%s: %s]", error.getField(), error.getDefaultMessage()));
    }

    log.warn("[유효성 검증 실패] {}", errorMessage);
    ApiResponse<Void> response = ApiResponse.failure(errorCode.getCode(), errorMessage.toString());
    return new ResponseEntity<>(response, errorCode.getHttpStatus());
  }

  // JSON 파싱 예외 처리
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadableException(
      HttpMessageNotReadableException e) {
    log.warn("[JSON 파싱 오류] {}", e.getMessage());

    ErrorCode errorCode = ErrorCode.VALIDATION_ERROR;
    String message = "요청 본문을 파싱할 수 없습니다. 올바른 JSON 형식인지 확인해주세요.";

    ApiResponse<Void> response = ApiResponse.failure(errorCode.getCode(), message);
    return new ResponseEntity<>(response, errorCode.getHttpStatus());
  }

  // 요청 파라미터 누락 예외 처리
  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ApiResponse<Void>> handleMissingServletRequestParameterException(
      MissingServletRequestParameterException e) {
    log.warn("[요청 파라미터 누락] {}", e.getMessage());

    ErrorCode errorCode = ErrorCode.VALIDATION_ERROR;
    String message = String.format("필수 파라미터 '%s'이(가) 누락되었습니다.", e.getParameterName());

    ApiResponse<Void> response = ApiResponse.failure(errorCode.getCode(), message);
    return new ResponseEntity<>(response, errorCode.getHttpStatus());
  }

  // 파라미터 타입 불일치 예외 처리
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ApiResponse<Void>> handleMethodArgumentTypeMismatchException(
      MethodArgumentTypeMismatchException e) {
    log.warn("[파라미터 타입 불일치] {}", e.getMessage());

    ErrorCode errorCode = ErrorCode.VALIDATION_ERROR;
    String message =
        String.format(
            "파라미터 '%s'의 타입이 올바르지 않습니다. 예상 타입: %s",
            e.getName(),
            e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "Unknown");

    ApiResponse<Void> response = ApiResponse.failure(errorCode.getCode(), message);
    return new ResponseEntity<>(response, errorCode.getHttpStatus());
  }

  // 허용되지 않는 HTTP 메소드 예외 처리
  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ApiResponse<Void>> handleHttpRequestMethodNotSupportedException(
      HttpRequestMethodNotSupportedException e) {
    log.warn("[허용되지 않는 HTTP 메소드] {}", e.getMessage());

    String message = String.format("지원하지 않는 HTTP 메소드입니다: %s", e.getMethod());
    ApiResponse<Void> response = ApiResponse.failure("00003", message);

    return new ResponseEntity<>(response, HttpStatus.METHOD_NOT_ALLOWED);
  }

  // 파일 크기 초과 예외 처리
  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ResponseEntity<ApiResponse<Void>> handleMaxUploadSizeExceededException(
      MaxUploadSizeExceededException e) {
    log.warn("[파일 크기 초과] {}", e.getMessage());

    ErrorCode errorCode = ErrorCode.VALIDATION_ERROR;
    String message = "업로드 파일 크기가 허용된 최대 크기를 초과하였습니다.";

    ApiResponse<Void> response = ApiResponse.failure(errorCode.getCode(), message);
    return new ResponseEntity<>(response, errorCode.getHttpStatus());
  }

  // 인증 예외 처리
  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(
      AuthenticationException e) {
    log.warn("[인증 실패] {}", e.getMessage());

    ApiResponse<Void> response = ApiResponse.failure("00004", "인증에 실패했습니다: " + e.getMessage());
    return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
  }

  // 권한 부족 예외 처리
  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException e) {
    log.warn("[접근 권한 부족] {}", e.getMessage());

    ApiResponse<Void> response = ApiResponse.failure("00005", "접근 권한이 없습니다: " + e.getMessage());
    return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
  }

  // 데이터 접근 예외 처리
  @ExceptionHandler(DataAccessException.class)
  public ResponseEntity<ApiResponse<Void>> handleDataAccessException(DataAccessException e) {
    log.error("[데이터베이스 오류] {}", e.getMessage(), e);

    ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
    String message = "데이터베이스 작업 중 오류가 발생했습니다.";

    ApiResponse<Void> response = ApiResponse.failure(errorCode.getCode(), message);
    return new ResponseEntity<>(response, errorCode.getHttpStatus());
  }

  // 기타 모든 예외 처리
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
    log.error("[서버 오류] 예상치 못한 예외 발생", e);

    ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
    String errorMessage = "서버 내부 오류가 발생했습니다.";

    ApiResponse<Void> response = ApiResponse.failure(errorCode.getCode(), errorMessage);
    return new ResponseEntity<>(response, errorCode.getHttpStatus());
  }
}
