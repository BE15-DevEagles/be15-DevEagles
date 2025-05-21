package com.deveagles.be15_deveagles_be.features.worklog.command.domain.exception;

import com.deveagles.be15_deveagles_be.common.exception.ErrorCodeType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum WorklogErrorCode implements ErrorCodeType {

  // 업무일지 관련 오류
  INVALID_WORKLOG_INPUT("60001", "업무일지 내용과 특이사항 모두 입력되어야 합니다.", HttpStatus.BAD_REQUEST),
  GEMINI_API_ERROR("60002", "Gemini API 호출 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  WORKLOG_ALREADY_EXISTS("60003", "이미 작성을 완료했습니다.", HttpStatus.BAD_REQUEST);
  private final String code;
  private final String message;
  private final HttpStatus httpStatus;
}
