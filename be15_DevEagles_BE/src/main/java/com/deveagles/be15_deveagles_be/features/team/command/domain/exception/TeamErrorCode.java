package com.deveagles.be15_deveagles_be.features.team.command.domain.exception;

import com.deveagles.be15_deveagles_be.common.exception.ErrorCodeType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public enum TeamErrorCode implements ErrorCodeType {
  TEAM_NAME_DUPLICATION("40001", "이미 존재하는 팀명입니다.", HttpStatus.CONFLICT),
  TEAM_NOT_FOUND("40002", "팀을 찾을 수 없을 수 없습니다.", HttpStatus.NOT_FOUND),
  NOT_TEAM_LEADER("40003", "팀장만 가능한 기능입니다.", HttpStatus.CONFLICT),
  ALREADY_TEAM_MEMBER("40004", "이미 팀에 존재하는 사용자입니다.", HttpStatus.CONFLICT),

  USER_NOT_FOUND("40030", "유저를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);

  private final String code;
  private final String message;
  private final HttpStatus httpStatus;
}
