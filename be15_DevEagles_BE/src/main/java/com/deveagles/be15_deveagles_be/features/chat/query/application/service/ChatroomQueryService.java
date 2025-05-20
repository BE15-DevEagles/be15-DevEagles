package com.deveagles.be15_deveagles_be.features.chat.query.application.service;

import com.deveagles.be15_deveagles_be.features.chat.query.application.dto.response.ChatroomListResponse;
import com.deveagles.be15_deveagles_be.features.chat.query.application.dto.response.ChatroomReadSummaryResponse;
import com.deveagles.be15_deveagles_be.features.chat.query.application.dto.response.ChatroomResponse;

public interface ChatroomQueryService {

  ChatroomListResponse getChatrooms(Long userId, String teamId, int page, int size);

  ChatroomResponse getChatroom(Long userId, String chatroomId);

  ChatroomReadSummaryResponse getChatroomReadSummary(Long userId, String chatroomId);
}
