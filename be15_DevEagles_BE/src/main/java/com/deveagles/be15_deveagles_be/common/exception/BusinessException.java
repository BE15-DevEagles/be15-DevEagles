package com.deveagles.be15_deveagles_be.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class BusinessException extends RuntimeException {

  private final ErrorCodeType errorCode;

  public BusinessException(ErrorCodeType errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }

  public BusinessException(ErrorCodeType errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
  }

  @Override
  public String getMessage() {
    return String.format("[%s] %s", errorCode.getCode(), super.getMessage());
  }

  public String getCode() {
    return errorCode.getCode();
  }

  public HttpStatus getHttpStatus() {
    return errorCode.getHttpStatus();
  }

  public String getOriginalMessage() {
    return super.getMessage();
  }
}
