package com.deveagles.be15_deveagles_be.features.chat.query.domain.repository;

import com.deveagles.be15_deveagles_be.features.chat.command.domain.aggregate.ChatMessage;
import java.util.List;
import java.util.Map;

public interface MessageQueryRepository {

  /**
   * 채팅방 메시지 조회
   *
   * @param chatroomId 채팅방 ID
   * @param beforeMessageId 해당 메시지 ID 이전의 메시지를 조회 (null인 경우 최신 메시지부터 조회)
   * @param limit 조회할 메시지 개수
   * @return 메시지 목록
   */
  List<ChatMessage> findMessages(String chatroomId, String beforeMessageId, int limit);

  /**
   * 메시지 읽음 상태 조회
   *
   * @param chatroomId 채팅방 ID
   * @param messageId 메시지 ID
   * @return 메시지 읽음 상태 정보와 사용자 정보 맵
   */
  Map<String, Object> getMessageReadStatus(String chatroomId, String messageId);
}
