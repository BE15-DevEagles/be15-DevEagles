package com.deveagles.be15_deveagles_be.features.chat.command.domain.exception;

import lombok.Getter;

@Getter
public class ChatBusinessException extends RuntimeException {

  private final ChatErrorCode errorCode;

  public ChatBusinessException(ChatErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }

  public ChatBusinessException(ChatErrorCode errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
  }

  public ChatBusinessException(ChatErrorCode errorCode, Throwable cause) {
    super(errorCode.getMessage(), cause);
    this.errorCode = errorCode;
  }
}
