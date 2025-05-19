package com.deveagles.be15_deveagles_be.features.todolist.exception;

import com.deveagles.be15_deveagles_be.common.exception.BusinessException;
import com.deveagles.be15_deveagles_be.common.exception.ErrorCodeType;

public class TodoNotFoundException extends BusinessException {

  public TodoNotFoundException(ErrorCodeType errorCode, String message) {
    super(errorCode, message);
  }

  public TodoNotFoundException(ErrorCodeType errorCode) {
    super(errorCode);
  }
}
