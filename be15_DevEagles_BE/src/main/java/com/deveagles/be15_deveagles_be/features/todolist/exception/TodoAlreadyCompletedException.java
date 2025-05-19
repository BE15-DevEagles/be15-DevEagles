package com.deveagles.be15_deveagles_be.features.todolist.exception;

import com.deveagles.be15_deveagles_be.common.exception.BusinessException;
import com.deveagles.be15_deveagles_be.common.exception.ErrorCodeType;

public class TodoAlreadyCompletedException extends BusinessException {

  public TodoAlreadyCompletedException(ErrorCodeType errorCode) {
    super(errorCode);
  }
}
