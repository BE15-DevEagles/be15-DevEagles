package com.deveagles.be15_deveagles_be.features.timecapsule.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum TimecapsuleErrorCode {
  NOT_FOUND(HttpStatus.NOT_FOUND, "800001", "타임캡슐을 찾을 수 없습니다."),
  FORBIDDEN(HttpStatus.FORBIDDEN, "800002", "해당 타임캡슐에 접근 권한이 없습니다."),
  INVALID_DATE(HttpStatus.BAD_REQUEST, "800003", "유효하지 않은 날짜입니다."),
  ALREADY_OPENED(HttpStatus.CONFLICT, "800004", "이미 오픈된 타임캡슐입니다."),
  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "800005", "사용자를 찾을 수 없습니다."),
  TEAM_NOT_FOUND(HttpStatus.NOT_FOUND, "800006", "팀을 찾을 수 없습니다."),
  INVALID_STATUS(HttpStatus.BAD_REQUEST, "800007", "잘못된 타임캡슐 상태입니다."),
  UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "800008", "로그인이 필요합니다."),
  INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "800009", "서버 내부 오류가 발생했습니다.");

  private final HttpStatus status;
  private final String code;
  private final String message;

  TimecapsuleErrorCode(HttpStatus status, String code, String message) {
    this.status = status;
    this.code = code;
    this.message = message;
  }
}
