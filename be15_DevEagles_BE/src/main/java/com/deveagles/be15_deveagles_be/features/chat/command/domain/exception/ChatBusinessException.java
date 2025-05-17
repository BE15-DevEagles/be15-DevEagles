package com.deveagles.be15_deveagles_be.features.chat.command.domain.exception;

import com.deveagles.be15_deveagles_be.common.exception.BusinessException;
import com.deveagles.be15_deveagles_be.common.exception.ErrorCode;
import lombok.Getter;

@Getter
public class ChatBusinessException extends BusinessException {

  private final ChatErrorCode chatErrorCode;

  public ChatBusinessException(ChatErrorCode errorCode) {
    super(convertToCommonErrorCode(errorCode));
    this.chatErrorCode = errorCode;
  }

  // ChatErrorCode를 ErrorCode로 변환
  private static ErrorCode convertToCommonErrorCode(ChatErrorCode chatErrorCode) {
    // 코드의 앞 부분에 따라 적절한 ErrorCode로 매핑
    String code = chatErrorCode.getCode();

    if (code.startsWith("301")) { // 메시지 관련 오류
      return ErrorCode.VALIDATION_ERROR;
    } else if (code.startsWith("302")) { // WebSocket 관련 오류
      return ErrorCode.INTERNAL_SERVER_ERROR;
    } else if (code.startsWith("303")) { // 파일 관련 오류
      if (code.equals("30301")) {
        return ErrorCode.FILE_SAVE_ERROR;
      } else {
        return ErrorCode.FILE_DELETE_ERROR;
      }
    }

    // 기본값으로 INTERNAL_SERVER_ERROR 반환
    return ErrorCode.INTERNAL_SERVER_ERROR;
  }
}
