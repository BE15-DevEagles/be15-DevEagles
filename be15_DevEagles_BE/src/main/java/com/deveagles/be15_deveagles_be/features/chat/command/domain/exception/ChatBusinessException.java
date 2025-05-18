package com.deveagles.be15_deveagles_be.features.chat.command.domain.exception;

import com.deveagles.be15_deveagles_be.common.exception.BusinessException;

public class ChatBusinessException extends BusinessException {

  public ChatBusinessException(ChatErrorCode errorCode) {
    super(errorCode);
  }

  public ChatBusinessException(ChatErrorCode errorCode, String message) {
    super(errorCode, message);
  }
}
