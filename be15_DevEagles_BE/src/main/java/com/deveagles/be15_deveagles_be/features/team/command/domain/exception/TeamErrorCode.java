package com.deveagles.be15_deveagles_be.features.team.command.domain.exception;

import com.deveagles.be15_deveagles_be.common.exception.ErrorCodeType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public enum TeamErrorCode implements ErrorCodeType {
  TEAM_NAME_DUPLICATION("40001", "이미 존재하는 팀명입니다.", HttpStatus.CONFLICT);

  private final String code;
  private final String message;
  private final HttpStatus httpStatus;
}
