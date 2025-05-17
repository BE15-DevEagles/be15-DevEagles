package com.deveagles.be15_deveagles_be.common.exception;

import org.springframework.http.HttpStatus;

public interface ErrorCodeType {
  String getCode();

  String getMessage();

  HttpStatus getHttpStatus();
}
