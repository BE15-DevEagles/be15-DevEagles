package com.deveagles.be15_deveagles_be.features.user.command.domain.exception;

import lombok.Getter;

@Getter
public class UserBusinessException extends RuntimeException {

  private final UserErrorCode userErrorCode;

  public UserBusinessException(UserErrorCode userErrorCode) {
    super(userErrorCode.getMessage());
    this.userErrorCode = userErrorCode;
  }
}
