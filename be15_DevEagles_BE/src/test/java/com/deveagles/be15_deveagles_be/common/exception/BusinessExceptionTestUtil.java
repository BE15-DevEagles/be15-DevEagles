package com.deveagles.be15_deveagles_be.common.exception;

import com.deveagles.be15_deveagles_be.features.chat.command.domain.exception.ChatBusinessException;
import com.deveagles.be15_deveagles_be.features.chat.command.domain.exception.ChatErrorCode;
import org.springframework.http.HttpStatus;

public class BusinessExceptionTestUtil {

  public static <T extends BusinessException> T createAndVerifyBusinessException(
      ErrorCodeType errorCode, Class<T> exceptionType) {
    if (exceptionType == ChatBusinessException.class) {
      ChatBusinessException exception = new ChatBusinessException((ChatErrorCode) errorCode);
      verifyBusinessException(exception, errorCode);
      return (T) exception;
    }

    throw new IllegalArgumentException("Unsupported exception type: " + exceptionType.getName());
  }

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

  public static void verifyHttpStatus(HttpStatus expected, HttpStatus actual) {
    if (expected != actual) {
      throw new AssertionError("Expected HTTP status: " + expected + ", but was: " + actual);
    }
  }
}
