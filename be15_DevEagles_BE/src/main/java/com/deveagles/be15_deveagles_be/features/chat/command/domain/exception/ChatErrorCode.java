package com.deveagles.be15_deveagles_be.features.chat.command.domain.exception;

import com.deveagles.be15_deveagles_be.common.exception.ErrorCodeType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public enum ChatErrorCode implements ErrorCodeType {
  // 채팅방 관련 오류
  CHAT_ROOM_NOT_FOUND("30001", "채팅방을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  CHAT_ROOM_ACCESS_DENIED("30002", "채팅방에 접근 권한이 없습니다.", HttpStatus.FORBIDDEN),
  DEFAULT_CHATROOM_ALREADY_EXISTS("30003", "이미 기본 채팅방이 존재합니다.", HttpStatus.BAD_REQUEST),
  DEFAULT_CHATROOM_CANNOT_BE_DELETED("30004", "기본 채팅방은 삭제할 수 없습니다.", HttpStatus.BAD_REQUEST),

  // 메시지 관련 오류
  MESSAGE_NOT_FOUND("30101", "메시지를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  MESSAGE_SEND_FAILED("30102", "메시지 전송에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  MESSAGE_DELETE_FAILED("30103", "메시지 삭제에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),

  // WebSocket 관련 오류
  WEBSOCKET_CONNECTION_FAILED("30201", "WebSocket 연결에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  WEBSOCKET_MESSAGE_FORMAT_INVALID("30202", "잘못된 메시지 형식입니다.", HttpStatus.BAD_REQUEST),

  // 파일 첨부 관련 오류
  FILE_UPLOAD_FAILED("30301", "파일 업로드에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  FILE_TYPE_NOT_ALLOWED("30302", "허용되지 않는 파일 형식입니다.", HttpStatus.BAD_REQUEST),
  FILE_SIZE_EXCEEDED("30303", "파일 크기가 제한을 초과했습니다.", HttpStatus.PAYLOAD_TOO_LARGE);

  private final String code;
  private final String message;
  private final HttpStatus httpStatus;
}
