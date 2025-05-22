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
  NOT_FOUND_USER_EXCEPTION("100003", "존재하지 않는 유저입니다.", HttpStatus.NOT_FOUND),

  // 프로필 저장
  PROFILE_SAVE_ERROR("100004", "프로필 저장에 실패했습니다.", HttpStatus.BAD_REQUEST),

  // 이메일 인증
  SEND_EMAIL_FAILURE_EXCEPTION("100100", "인증 메일 전송이 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  DUPLICATE_SEND_AUTH_EXCEPTION(
      "1000101", "이미 인증 메일을 발송했습니다. 잠시 후 다시 시도해주세요.", HttpStatus.BAD_REQUEST),
  INVALID_AUTH_CODE("1000102", "유효한 인증이 아닙니다.", HttpStatus.BAD_REQUEST);

  private final String code;
  private final String message;
  private final HttpStatus httpStatus;
}
