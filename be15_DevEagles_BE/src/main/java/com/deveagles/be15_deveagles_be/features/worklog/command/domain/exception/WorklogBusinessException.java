package com.deveagles.be15_deveagles_be.features.worklog.command.domain.exception;

import com.deveagles.be15_deveagles_be.common.exception.BusinessException;

public class WorklogBusinessException extends BusinessException {
  public WorklogBusinessException(WorklogErrorCode errorCode) {
    super(errorCode);
  }

  public WorklogBusinessException(WorklogErrorCode errorCode, String message) {
    super(errorCode, message);
  }
}
