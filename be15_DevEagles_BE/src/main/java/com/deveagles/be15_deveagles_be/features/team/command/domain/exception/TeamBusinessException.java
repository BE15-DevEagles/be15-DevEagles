package com.deveagles.be15_deveagles_be.features.team.command.domain.exception;

import com.deveagles.be15_deveagles_be.common.exception.BusinessException;

public class TeamBusinessException extends BusinessException {

  public TeamBusinessException(TeamErrorCode errorCode) {
    super(errorCode, errorCode.getMessage());
  }
}
