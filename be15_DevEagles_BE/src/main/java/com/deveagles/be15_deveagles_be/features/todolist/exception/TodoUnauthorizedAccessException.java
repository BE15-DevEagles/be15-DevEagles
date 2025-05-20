package com.deveagles.be15_deveagles_be.features.todolist.exception;

import com.deveagles.be15_deveagles_be.common.exception.BusinessException;
import com.deveagles.be15_deveagles_be.common.exception.ErrorCodeType;

public class TodoUnauthorizedAccessException extends BusinessException {

  public TodoUnauthorizedAccessException(ErrorCodeType errorCode) {
    super(errorCode);
  }

  public TodoUnauthorizedAccessException(ErrorCodeType errorCode, String message) {
    super(errorCode, message);
  }
}
