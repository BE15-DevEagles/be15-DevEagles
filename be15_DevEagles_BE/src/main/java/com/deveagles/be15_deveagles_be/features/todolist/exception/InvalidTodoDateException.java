package com.deveagles.be15_deveagles_be.features.todolist.exception;

import com.deveagles.be15_deveagles_be.common.exception.BusinessException;
import com.deveagles.be15_deveagles_be.common.exception.ErrorCodeType;

public class InvalidTodoDateException extends BusinessException {

  public InvalidTodoDateException(ErrorCodeType errorCode) {
    super(errorCode);
  }

  public InvalidTodoDateException(ErrorCodeType errorCode, String message) {
    super(errorCode, message);
  }
}
