package com.deveagles.be15_deveagles_be.features.comment.command.domain.exception;

import com.deveagles.be15_deveagles_be.common.exception.ErrorCodeType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CommentErrorCode implements ErrorCodeType {
  INVALID_UPDATE_COMMENT("60101", "댓글을 수정할 수 없습니다.", HttpStatus.CONFLICT),
  INVALID_REQUEST("60103", "잘못된 요청입니다.", HttpStatus.BAD_REQUEST),
  NO_PERMISSION("60105", "권한이 없습니다.", HttpStatus.UNAUTHORIZED);

  private final String code;
  private final String message;
  private final HttpStatus httpStatus;
}
