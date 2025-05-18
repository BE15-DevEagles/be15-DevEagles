package com.deveagles.be15_deveagles_be.common.exception;

import com.deveagles.be15_deveagles_be.features.chat.command.domain.exception.ChatBusinessException;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.exception.ChatErrorCode;
import org.springframework.http.HttpStatus;

/** 테스트에 사용되는 유틸리티 클래스 */
public class BusinessExceptionTestUtil {

  /**
   * 비즈니스 예외를 생성하고 검증합니다.
   *
   * @param errorCode 에러 코드
   * @return 비즈니스 예외
   */
  public static <T extends BusinessException> T createAndVerifyBusinessException(
      ErrorCodeType errorCode, Class<T> exceptionType) {
    if (exceptionType == ChatBusinessException.class) {
      ChatBusinessException exception = new ChatBusinessException((ChatErrorCode) errorCode);
      verifyBusinessException(exception, errorCode);
      return (T) exception;
    }

    throw new IllegalArgumentException("Unsupported exception type: " + exceptionType.getName());
  }

  /**
   * 비즈니스 예외를 검증합니다.
   *
   * @param exception 비즈니스 예외
   * @param errorCode 에러 코드
   */
  public static void verifyBusinessException(BusinessException exception, ErrorCodeType errorCode) {
    if (!exception.getCode().equals(errorCode.getCode())) {
      throw new AssertionError(
          "Expected error code: " + errorCode.getCode() + ", but was: " + exception.getCode());
    }

    if (!exception.getOriginalMessage().equals(errorCode.getMessage())) {
      throw new AssertionError(
          "Expected message: "
              + errorCode.getMessage()
              + ", but was: "
              + exception.getOriginalMessage());
    }

    if (!exception.getHttpStatus().equals(errorCode.getHttpStatus())) {
      throw new AssertionError(
          "Expected HTTP status: "
              + errorCode.getHttpStatus()
              + ", but was: "
              + exception.getHttpStatus());
    }
  }

  /**
   * HTTP 상태 코드를 검증합니다.
   *
   * @param expected 예상 HTTP 상태 코드
   * @param actual 실제 HTTP 상태 코드
   */
  public static void verifyHttpStatus(HttpStatus expected, HttpStatus actual) {
    if (expected != actual) {
      throw new AssertionError("Expected HTTP status: " + expected + ", but was: " + actual);
    }
  }
}
