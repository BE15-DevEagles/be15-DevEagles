package com.deveagles.be15_deveagles_be.features.user.command.domain.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public enum UserErrorCode {

  // 회원 가입
  DUPLICATE_USER_EMAIL_EXCEPTION("100001", "중복된 아이디입니다.", HttpStatus.BAD_REQUEST),
  DUPLICATE_USER_PHONE_EXCEPTION("100002", "중복된 전화번호입니다.", HttpStatus.BAD_REQUEST),

  // 유저 조회
  NOT_FOUND_USER_EXCEPTION("100003", "존재하지 않는 유저입니다.", HttpStatus.NOT_FOUND);

  private final String code;
  private final String message;
  private final HttpStatus httpStatus;
}
